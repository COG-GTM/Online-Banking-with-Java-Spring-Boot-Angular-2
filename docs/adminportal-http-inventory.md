# AdminPortal HTTP call-site inventory (COG-1147)

Read-only audit for the Spring Boot 1.5.4 → 4.1 migration. It lists every HTTP call the Angular
AdminPortal (`AdminPortal/src/app/**`) makes to UserFront and matches each one to the Spring mapping it hits.

- Audited revision: `master` @ `260093a`. Line numbers refer to that commit.
- Angular: `@angular/http` / `@angular/common` 4.4.7 (the versions `npm install` resolves from `^4.0.0`).
- Server behaviour was observed on a live Boot 1.5.4 app (JDK 8, MySQL 5.7). See [Evidence](#evidence).

**Consumers:** COG-1160 (W1 AdminPortal XSRF + trailing slash), COG-1153 / COG-1154 (GET→POST mutations),
COG-1159 (`/api/csrf`), COG-1165 / COG-1166 (W2 CORS + CSRF), COG-1167 / COG-1175 / COG-1180 (date
contract), COG-1171 (W3 URL-matching audit).

## TL;DR

1. **No trailing slashes.** None of the 9 AdminPortal URL literals ends in `/`. COG-1160 item 1 is a no-op
   for AdminPortal. The server-side Thymeleaf templates have no trailing-slash links either. The only one
   is `@{/}` (root), which is unaffected.
2. **3 state-changing calls use GET** against mappings that declare no `method` (C6, C7, C8). These must become
   POST before CSRF is turned on.
3. **Any call that adds `X-XSRF-TOKEN` triggers a CORS preflight.** Today's `RequestFilter` preflight response
   does **not** allow `x-xsrf-token`, so that call fails in the browser against the unmodified Boot 1.5.4
   server. See M6.
4. **Dates:** no AdminPortal code string-matches, regexes or `substring`s a date. Every date read goes through
   Angular's `DatePipe` in a template. Boot 1.5.4 currently sends dates as **epoch-millis numbers**, not ISO
   strings. `DatePipe` renders numbers, `+0000`, `+00:00` and `Z` identically. An offset-less
   `LocalDateTime` string (COG-1180) is rendered in the **browser's** zone.
5. `withCredentials: true` is set on **all 9** calls. No call adds a non-simple header today, so **no call
   triggers a preflight today**.

## Call-site inventory

All URLs are hardcoded absolute `http://localhost:8080/...` strings (9 occurrences). None come from
`environment.ts`. Components never call `Http` directly. Every call goes through one of the three services
below.

| ID | Service method (file:line of URL / call) | Verb sent | URL literal exactly as written | Trailing `/` | `withCredentials` | Extra headers / body | Called from |
|----|------------------------------------------|-----------|--------------------------------|--------------|-------------------|----------------------|-------------|
| C1 | `LoginService.sendCredential` — `login.service.ts:11` / `:18` | POST | `'http://localhost:8080/index'` | no | yes | `Content-Type: application/x-www-form-urlencoded`. Body is `'username='+username+'&password='+password` (**not URL-encoded**, line 12) | `login.component.ts:25` |
| C2 | `LoginService.logout` — `login.service.ts:22` / `:23` | GET | `'http://localhost:8080/logout'` | no | yes | — | `navbar.component.ts:23` |
| C3 | `UserService.getUsers` — `user.service.ts:11` / `:12` | GET | `"http://localhost:8080/api/user/all"` | no | yes | — | `user-account.component.ts:19` |
| C4 | `UserService.getPrimaryTransactionList` — `user.service.ts:16` / `:17` | GET | `"http://localhost:8080/api/user/primary/transaction?username="+username` | no | yes | query param **not URL-encoded** | `primary-transaction.component.ts:24` |
| C5 | `UserService.getSavingsTransactionList` — `user.service.ts:21` / `:22` | GET | `"http://localhost:8080/api/user/savings/transaction?username="+username` | no | yes | query param **not URL-encoded** | `savings-transaction.component.ts:25` |
| C6 | `UserService.enableUser` — `user.service.ts:26` / `:27` | GET | `"http://localhost:8080/api/user/"+username+"/enable"` | no | yes | — | `user-account.component.ts:36` |
| C7 | `UserService.disableUser` — `user.service.ts:31` / `:32` | GET | `"http://localhost:8080/api/user/"+username+"/disable"` | no | yes | — | `user-account.component.ts:41` |
| C8 | `AppointmentService.confirmAppointment` — `appointment.service.ts:16` / `:17` | GET | `"http://localhost:8080/api/appointment/"+id+"/confirm"` | no | yes | — | `appointment.component.ts:28` |
| C9 | `AppointmentService.getAppointmentList` — `appointment.service.ts:11` / `:12` | GET | `"http://localhost:8080/api/appointment/all"` | no | yes | — | `appointment.component.ts:19` |

(C8 and C9 are numbered by risk, not file order.)

## Server mapping cross-reference

Paths are relative to `UserFront/src/main/java/com/userFront/`.

| ID | Server handler (file:line) | Declared `method` | Security | Observed on Boot 1.5.4 | Boot 6+/Security 6+ exposure |
|----|----------------------------|-------------------|----------|------------------------|------------------------------|
| C1 | Spring Security `UsernamePasswordAuthenticationFilter`. The processing URL defaults to `loginPage("/index")` (`config/SecurityConfig.java:61`). `HomeController.java:35` `@RequestMapping("/index")` serves the GET login page. | filter: POST only. `HomeController /index`: **none** | `permitAll` via `formLogin().permitAll()` | Good creds: `302 → /userFront`, XHR follows to `200 text/html`. Bad creds: `302 → /index?error`, followed to `200 text/html`. | With CSRF on (COG-1166), 403 unless `X-XSRF-TOKEN` is sent **and** the cookie was bootstrapped first (COG-1159/COG-1160). The redirected `/userFront` response must also carry CORS headers, which is why COG-1165 registers `/**`. |
| C2 | `LogoutFilter` with `AntPathRequestMatcher("/logout")` (`config/SecurityConfig.java:63`) | **any** (matcher has no method) | `permitAll` | `GET` and `POST` both `302 → /index?logout`, and the session is invalidated | `AntPathRequestMatcher` is removed in Security 7. COG-1173 swaps it for `PathPatternRequestMatcher.withDefaults().matcher("/logout")`, which still matches any method, so GET keeps working. Only when W5 moves to POST-only logout does C2 have to change. |
| C3 | `resource/UserResource.java:30` (class prefix `/api` at `:20`) | GET | `@PreAuthorize("hasRole('ADMIN')")` (`:21`) | 200 JSON. `POST` → 405 | none |
| C4 | `resource/UserResource.java:35` | GET | ADMIN | 200 JSON | trailing-slash variant `/api/user/primary/transaction/` 200 today, 404 on Spring 6. The client doesn't use it. |
| C5 | `resource/UserResource.java:40` | GET | ADMIN | 200 JSON | as C4 |
| C6 | `resource/UserResource.java:45` `@RequestMapping("/user/{username}/enable")` | **none** → answers every verb | ADMIN | GET/POST/PUT/DELETE all 200, empty body | GET mutation bypasses `CsrfFilter` once CSRF is on → COG-1154 makes it `@PostMapping`, and after that GET → 405 |
| C7 | `resource/UserResource.java:50` `@RequestMapping("/user/{username}/disable")` | **none** | ADMIN | GET/POST/PUT/DELETE all 200, empty body | as C6 (COG-1154) |
| C8 | `resource/AppointmentResource.java:29` `@RequestMapping("/{id}/confirm")` (class prefix `/api/appointment` at `:15`) | **none** | ADMIN (`:16`) | GET/POST 200, empty body | as C6 (COG-1153) |
| C9 | `resource/AppointmentResource.java:22` `@RequestMapping("/all")` | **none** → answers every verb | ADMIN | GET 200 JSON. `POST` also 200. | Read-only, so no CSRF risk. Optional hardening: `@GetMapping`. No client change needed. |

Common behaviour for C3–C9, observed today:

- Anonymous: `302 → /index`. The XHR follows that to the HTML login page.
- `ROLE_USER`: `403`.
- Every response carries `Access-Control-Allow-Origin: http://localhost:4200` and `Access-Control-Allow-Credentials: true`
  from `config/RequestFilter.java:23-27`. `SecurityConfig` has `.cors().disable()` (`:60`).

**Trailing-slash matching today:** every endpoint above (and `/userFront/`) also answers with a trailing
slash on Boot 1.5.4 (all returned 200). Spring 6 turns that off. AdminPortal never sends the slash form, so
the client is not exposed.

## Preflight / CORS behaviour

- **Today no AdminPortal call is preflighted.** All calls are GET, or a POST with the CORS-safelisted
  `application/x-www-form-urlencoded`, and none add custom headers. `withCredentials` does not by itself
  cause a preflight.
- `RequestFilter` answers `OPTIONS` itself and short-circuits the chain (`config/RequestFilter.java:29-41`). Observed response:
  `200`, `Access-Control-Allow-Methods: POST,GET,DELETE` and
  `Access-Control-Allow-Headers: authorization, content-type, access-control-request-headers, access-control-request-method, accept, origin, authorization, x-requested-with`.
  **`x-xsrf-token` is not in that list.**
- As soon as any call adds `X-XSRF-TOKEN` (COG-1160, COG-1153, COG-1154), the browser sends a preflight. On
  Boot 1.5.4 that preflight comes back without `x-xsrf-token` allowed, so **the browser blocks the real
  request**. See M6.
- After COG-1165, Spring's `CorsFilter` answers preflights. Its proposed allowed-headers list already has
  `x-xsrf-token`.

## Response consumption (string matching / date parsing)

| Component (file:line) | How the body is read | Fields read in template | Date handling |
|-----------------------|----------------------|-------------------------|---------------|
| `user-account.component.ts:21` | `JSON.parse(JSON.parse(JSON.stringify(res))._body)` | `username, firstName, lastName, email, phone, primaryAccount.accountBalance, savingsAccount.accountBalance, enabled` (`user-account.component.html:19-26`) | none |
| `primary-transaction.component.ts:27` | same `_body` pattern (also `console.log` at `:26`) | `date, description, type, status, amount, availableBalance` (`primary-transaction.component.html:16-21`) | `{{primaryTransaction.date \| date: 'MM/dd/yyyy'}}` (`:16`) |
| `savings-transaction.component.ts:28` | same | same (`savings-transaction.component.html:16-21`) | `{{savingsTransaction.date \| date: 'MM/dd/yyyy'}}` (`:16`) |
| `appointment.component.ts:21` | same | `id, user.username, date, description, confirmed` (`appointment.component.html:16-21`) | `{{appointment.date \| date: 'MM/dd/yyyy - hh:mm'}}` (`:18`) |
| `login.component.ts:26-29` | body ignored. **Any** success-path response counts as a successful login | — | — |
| `navbar.component.ts:24-26`, C6–C8 callers | body ignored | — | — |

Findings:

- **No application code string-matches, regexes, `substring`s or `new Date(...)`s any response field.** Dates
  are only ever passed to Angular's `DatePipe`.
- Internally, `DatePipe` 4.4.7 calls `new Date(n)` for numbers and numeric strings. For other strings it matches
  `ISO8601_DATE_REGEX`
  (`/^(\d{4})-?(\d\d)-?(\d\d)(?:T(\d\d)(?::?(\d\d)(?::?(\d\d)(?:\.(\d+))?)?)?(Z|([+-])(\d\d):?(\d\d))?)?$/`,
  `node_modules/@angular/common/@angular/common.es5.js:3388`). The regex accepts `+0000`, `+00:00` and `Z`.
  Verified by running the real pipe:

  | input | `TZ=UTC` | `TZ=America/New_York` |
  |-------|----------|-----------------------|
  | `1497972600000` (Boot 1.5.4 today) | 06/20/2017 - 03:30 | 06/20/2017 - 11:30 |
  | `"2017-06-20T15:30:00.000+0000"` (Jackson < 2.11) | 06/20/2017 - 03:30 | 06/20/2017 - 11:30 |
  | `"2017-06-20T15:30:00.000+00:00"` (Jackson ≥ 2.11 / Boot 2.4+) | 06/20/2017 - 03:30 | 06/20/2017 - 11:30 |
  | `"2017-06-20T15:30:00.000Z"` | 06/20/2017 - 03:30 | 06/20/2017 - 11:30 |
  | `"2017-06-20T15:30:00"` (offset-less `LocalDateTime`, COG-1180) | 06/20/2017 - 03:30 | **06/20/2017 - 03:30** (interpreted as browser-local, so the displayed instant shifts) |

- **Boot 1.5.4 currently serializes `java.util.Date` as epoch millis**, e.g. `"date":1790785026000` from
  `/api/user/primary/transaction` and `"date":1497929400000` from `/api/appointment/all`. It does not send an
  ISO string. From Boot 2.0 the default switches to ISO-8601 strings. Implications:
  - COG-1145's golden fixtures will contain numbers. The parse-equivalence comparator must accept
    number vs. ISO string, not only `+0000` vs `+00:00`.
  - COG-1167's premise ("Boot has disabled `WRITE_DATES_AS_TIMESTAMPS` since 1.2 … today's output is ISO") does
    not match the observed 1.5.4 output. Its conclusion still holds: AdminPortal renders both forms the same
    way, so no client change and no `write-dates-as-timestamps` pin are needed.
- **`date: 'MM/dd/yyyy - hh:mm'`** (`appointment.component.html:18`) uses the 12-hour `hh` field with no `a`
  marker, so 03:30 and 15:30 look identical. This is display-only and exists today. It is related to, but
  separate from, the server-side `hh` parse bug in COG-1180.
- The `._body` pattern relies on a private field of Angular's `Response`. It is not affected by the server
  migration. If a response is not JSON (for example an expired session, where the XHR follows the 302 to the
  HTML login page), `JSON.parse` throws inside the `next` callback. That exception is uncaught; the `error`
  handler never runs.
- `/api/user/all` and the nested `user` in `/api/appointment/all` also contain `password` (a bcrypt hash),
  `recipientList` and `authorities`. AdminPortal reads **none** of these. The W1 ticket that removes
  `recipientList` via `@JsonIgnore` is client-safe.

## Must change

Required so AdminPortal keeps working through the migration. "Required value" is the target code at that line.
`xsrfOptions()` is the COG-1160 helper that returns `{ headers, withCredentials: true }`, with
`X-XSRF-TOKEN` taken from the `XSRF-TOKEN` cookie.

| # | File:line | Current value | Required value | Owner ticket | Why / trigger |
|---|-----------|---------------|----------------|--------------|---------------|
| M1 | `AdminPortal/src/app/user.service.ts:27` | `return this.http.get(url, { withCredentials: true });` | `return this.http.post(url, null, xsrfOptions());` | COG-1154 (with `UserResource.java:45` → `@PostMapping`) | GET mutation bypasses CSRF. The server GET form becomes 405. |
| M2 | `AdminPortal/src/app/user.service.ts:32` | `return this.http.get(url, { withCredentials: true });` | `return this.http.post(url, null, xsrfOptions());` | COG-1154 (with `UserResource.java:50` → `@PostMapping`) | as M1 |
| M3 | `AdminPortal/src/app/appointment.service.ts:17` | `return this.http.get(url, { withCredentials: true });` | `return this.http.post(url, null, xsrfOptions());` | COG-1153 (with `AppointmentResource.java:29` → `@PostMapping`) | as M1 |
| M4 | `AdminPortal/src/app/login.service.ts:18` | `return this.http.post(url, params, {headers: headers, withCredentials : true});` | same call, with `X-XSRF-TOKEN` appended to `headers` (keep `Content-Type: application/x-www-form-urlencoded`) | COG-1160 | Required once COG-1166 enables CSRF: the login POST is otherwise rejected with 403 |
| M5 | `AdminPortal/src/app/login.service.ts:10-11` (start of `sendCredential`, before the POST) | *(no call)* | `GET 'http://localhost:8080/api/csrf'` with `{ withCredentials: true }`, then chain the POST in M4 | COG-1160 (server endpoint: COG-1159) | On a cold browser no `XSRF-TOKEN` cookie exists yet, so the first POST gets a 403 |
| M6 | the COG-1160 `xsrfOptions()` helper (new code in `AdminPortal/src/app/`) | *(n/a)* | Add `X-XSRF-TOKEN` **only when the `XSRF-TOKEN` cookie is present**. Never send an empty header. | COG-1160 | A custom header forces a preflight. Today's `RequestFilter.java:39` `Access-Control-Allow-Headers` lacks `x-xsrf-token`, so an unconditional header breaks M1–M4 on the unmodified Boot 1.5.4 server (fails COG-1160's own acceptance gate). While CSRF is off there's no cookie, so there's no header and no preflight. **Ordering:** COG-1165 (CorsConfig with `x-xsrf-token`) must land before or with COG-1166. Otherwise the cookie appears while `RequestFilter` is still answering preflights. |

**Trailing slashes: nothing to change.** No AdminPortal literal ends in `/`, so COG-1160 item 1 has no work.

**`withCredentials`: nothing to change.** It is already on all 9 calls. Keep it on the new `/api/csrf` call (M5)
and on every rewritten call (M1–M4).

### Conditional (only when the named server change lands)

| # | File:line | Current value | Required value | Trigger |
|---|-----------|---------------|----------------|---------|
| K1 | `AdminPortal/src/app/login.service.ts:23` | `return this.http.get(url, { withCredentials: true });` (`GET /logout`) | `return this.http.post(url, null, xsrfOptions());` | Only when W5 switches to POST-only logout. COG-1173 keeps any-method matching, so no change before then. |
| K2 | `appointment.component.html:18` / any date rendering | `appointment.date \| date: ...` | Unchanged if the server keeps an offset (or `Z`). If COG-1180 ships an offset-less `LocalDateTime`, either serialize with an offset/`Z` on the server or accept browser-local display. | COG-1180 |

## Should change (pre-existing defects; not required by the migration)

These affect how reliably the checks above can be verified, so listing them helps the W2/W3 walkthroughs. Fixing
them is optional and not in scope for this ticket.

- `user-account.component.ts:36-37`, `:41-42`, `appointment.component.ts:28-29`, `navbar.component.ts:23-29` call
  `subscribe()` and then run `location.reload()` straight away, without waiting for the response. The reload
  can abort the in-flight XHR. After M1–M3 add a preflight (two round trips), the abort becomes more likely.
  Move the reload into the `next` callback.
- `login.component.ts:26-29` treats any success-path response as a login. A wrong password returns
  `302 → /index?error → 200 text/html`, so the portal marks itself logged in (`PortalAdminHasLoggedIn = 'true'`).
- `login.service.ts:12`, `user.service.ts:16`, `:21`, `:26`, `:31`: user input is concatenated into the body
  or URL without `encodeURIComponent`.
- All 9 URLs hardcode `http://localhost:8080`. There is no `environment.apiBase`.

## Evidence

The live Boot 1.5.4 app ran on JDK 8 (Temurin 1.8.0_504) against MySQL 5.7 in Docker, with
`--spring.datasource.url=jdbc:mysql://localhost:3306/onlinebanking?useSSL=false`. Seed data: users `admin`
(ROLE_ADMIN) and `alice` (ROLE_USER), one primary deposit, one savings deposit and one appointment. Every
request below was sent with `Origin: http://localhost:4200` and a cookie jar, the way the browser sends them.

```
C1 POST /index (good creds)          302 Location: /userFront, ACAO: http://localhost:4200, ACAC: true, Set-Cookie JSESSIONID
C1 POST /index (bad creds, followed) final 200 /index?error text/html (1 redirect)
C3 GET /api/user/all                 200 application/json
C4 GET /api/user/primary/transaction 200 [{"id":1,"date":1790785026000,...}]
C5 GET /api/user/savings/transaction 200 [{"id":1,"date":1790785026000,...}]
C9 GET /api/appointment/all          200 [{"id":1,"date":1497929400000,...,"user":{...}}]
anon GET /api/user/all               302 -> /index        ROLE_USER GET /api/user/all  403
C7 GET|POST|PUT|DELETE .../disable   200 200 200 200
C6 GET|POST .../enable               200 200
C8 POST|GET .../1/confirm            200 200
POST /api/user/all                   405   (method declared)
POST /api/appointment/all            200   (no method declared)
trailing slash: /api/user/all/, /api/user/primary/transaction/, /api/user/savings/transaction/,
  /api/user/alice/enable/, /api/user/alice/disable/, /api/appointment/all/, /api/appointment/1/confirm/,
  /userFront/                        all 200 on Boot 1.5.4
OPTIONS .../1/confirm (ACRH: x-xsrf-token)  200, Allow-Methods: POST,GET,DELETE,
  Allow-Headers: authorization, content-type,access-control-request-headers,access-control-request-method,
  accept,origin,authorization,x-requested-with          <- no x-xsrf-token
C2 POST /logout | GET /logout        302 -> /index?logout, then /api/user/all 302 (logged out) for both
```

The DatePipe table above came from running `new DatePipe('en-US').transform(v, 'MM/dd/yyyy - hh:mm')`
out of `node_modules/@angular/common/bundles/common.umd.js` 4.4.7 on Node 12, under `TZ=UTC` and
`TZ=America/New_York`.
