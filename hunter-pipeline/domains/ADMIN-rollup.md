# ADMIN Domain Rollup — Admin API

**Repo:** COG-GTM/Online-Banking-with-Java-Spring-Boot-Angular-2
**Domain:** ADMIN (Admin API)
**Stack:** Java 8, Spring Boot 1.5.4 (EOL), Spring Security, Angular 4 admin portal, MySQL
**Exposure:** external-auth (ROLE_ADMIN)
**Vuln classes scanned:** AUTHZ, IDOR, PRIVESC, SDE
**Scan date:** 2026-06-04

---

## 1. Domain Brief

The ADMIN domain is the administrative surface of an online banking platform. It is implemented as
two Spring `@RestController` resources under `/api`, consumed by a standalone Angular 4 single-page
admin portal (`AdminPortal/`). Administrators authenticate against the same `UserFront` Spring Boot
backend used by retail customers (shared form-login + session cookie), and are distinguished only by
the `ROLE_ADMIN` authority.

### Components

| Component | File | Role |
|---|---|---|
| User Admin API | `UserFront/src/main/java/com/userFront/resource/UserResource.java` | List users, view any user's transactions, enable/disable any account |
| Appointment Admin API | `UserFront/src/main/java/com/userFront/resource/AppointmentResource.java` | List all appointments, confirm any appointment |
| Security config | `UserFront/src/main/java/com/userFront/config/SecurityConfig.java` | Form login, CSRF/CORS disabled, method security, BCrypt |
| CORS filter | `UserFront/src/main/java/com/userFront/config/RequestFilter.java` | Hardcoded CORS w/ credentials |
| User entity | `UserFront/src/main/java/com/userFront/domain/User.java` | JPA + Jackson-serialized model returned by `/api/user/all` |
| Admin SPA services | `AdminPortal/src/app/{user,appointment,login}.service.ts` | HTTP clients calling the Admin API |
| Admin SPA auth gate | `AdminPortal/src/app/login/login.component.ts`, `app.routing.ts` | localStorage flag, no route guards |

### Endpoints (all under `/api`)

| Method (effective) | Path | Handler | Action |
|---|---|---|---|
| GET | `/api/user/all` | `UserResource.userList` | Returns **all** `User` entities |
| GET | `/api/user/primary/transaction?username=` | `UserResource.getPrimaryTransactionList` | Any user's primary transactions |
| GET | `/api/user/savings/transaction?username=` | `UserResource.getSavingsTransactionList` | Any user's savings transactions |
| ANY (incl. GET) | `/api/user/{username}/enable` | `UserResource.enableUser` | Enable any account |
| ANY (incl. GET) | `/api/user/{username}/disable` | `UserResource.diableUser` | Disable any account |
| ANY (incl. GET) | `/api/appointment/all` | `AppointmentResource.findAppointmentList` | All appointments |
| ANY (incl. GET) | `/api/appointment/{id}/confirm` | `AppointmentResource.confirmAppointment` | Confirm any appointment |

### Authorization model (as-built)

- `@PreAuthorize("hasRole('ADMIN')")` is declared **at the class level only** on both resources;
  `@EnableGlobalMethodSecurity(prePostEnabled=true)` is set, so this is the *only* enforcement layer.
- The HTTP-layer config (`SecurityConfig.configure`) has **no** `antMatchers` scoping `/api/**` to
  `ROLE_ADMIN`; it ends in `.anyRequest().authenticated()`. So URL-level security only requires *any*
  authenticated principal — admin enforcement depends entirely on method security working.
- Authorization is **role-coarse**: there is no per-object / per-tenant scoping. Any ROLE_ADMIN
  principal may read or mutate **any** user's data. There is no audit logging on privileged actions.
- `csrf().disable()` and `cors().disable()` are set; CORS is instead handled by a custom
  `RequestFilter` that emits `Access-Control-Allow-Credentials: true`.
- State-changing operations (enable/disable/confirm) are mapped with bare `@RequestMapping` and are
  therefore reachable via **GET**.

---

## 2. Vulnerability Candidates

> ID format: `ADMIN-{VULN}-{NNN}`. Candidates are over-generated; some overlap and some are
> defense-in-depth observations. Severity is a triage estimate (Critical/High/Medium/Low).

### AUTHZ — Authorization / access control

#### ADMIN-AUTHZ-001 — `/api/**` has no URL-level role restriction; admin enforcement is single-layered
- **Severity:** High
- **Files:** `config/SecurityConfig.java:51-66`, `resource/UserResource.java:19-22`, `resource/AppointmentResource.java:14-17`
- **Description:** The HTTP security chain only enforces `.anyRequest().authenticated()`; there is no
  `antMatchers("/api/**").hasRole("ADMIN")`. Admin gating relies solely on class-level
  `@PreAuthorize`. If method security is ever broken (proxy/CGLIB issues, a refactor to `final`
  methods, accidental removal of `@EnableGlobalMethodSecurity`, or a Spring upgrade changing
  defaults), every `/api` endpoint silently degrades to "any authenticated user" — i.e. any
  ROLE_USER customer could call the admin API.
- **Exploit scenario:** A regular customer authenticates, then issues `GET /api/user/all`. With
  method security intact they get 403; if it regresses, they receive every user record. No
  second layer (URL rules) prevents this.

#### ADMIN-AUTHZ-002 — Class-level `@PreAuthorize` is the sole authorization control (no defense in depth)
- **Severity:** Medium
- **Files:** `resource/UserResource.java:21`, `resource/AppointmentResource.java:16`
- **Description:** Authorization is asserted once, at the type level. There is no method-level
  re-assertion for the most dangerous operations (enable/disable/confirm), no service-layer check,
  and no URL-layer check. A single annotation is the entire control surface for all admin power.
- **Exploit scenario:** Any change that drops or shadows the class annotation (e.g. adding a new
  endpoint to a different controller, or a copy-paste of a method into an unannotated class)
  exposes privileged behaviour with no backstop.

#### ADMIN-AUTHZ-003 — CSRF disabled on cookie-authenticated state-changing admin endpoints
- **Severity:** High
- **Files:** `config/SecurityConfig.java:60`, `config/RequestFilter.java:23-27`, `resource/UserResource.java:45-53`, `resource/AppointmentResource.java:29-32`
- **Description:** `csrf().disable()` is combined with session-cookie auth, `rememberMe()`, and a CORS
  filter that returns `Access-Control-Allow-Credentials: true`. State-changing operations
  (`enable`, `disable`, `confirm`) are reachable via simple GET requests. A logged-in admin visiting
  a malicious page can be forced to perform privileged mutations cross-site.
- **Exploit scenario:** An attacker emails an admin a link/page containing
  `<img src="http://bank:8080/api/user/victim/disable">`. While the admin's session cookie is valid,
  the browser silently disables (or enables) the targeted account. No token or same-origin check
  blocks it.

#### ADMIN-AUTHZ-004 — State-changing actions exposed over GET (unsafe HTTP method)
- **Severity:** Medium
- **Files:** `resource/UserResource.java:45-53`, `resource/AppointmentResource.java:29-32`
- **Description:** `enableUser`, `disableUser`, and `confirmAppointment` use bare `@RequestMapping`
  with no `method = POST`. GET is therefore allowed for mutating operations, enabling CSRF via
  `<img>`/prefetch, accidental triggering via crawlers/link-prefetchers, and caching of side-effects.
- **Exploit scenario:** A browser prefetch, a chat-link unfurler, or a security scanner crawling the
  SPA's links issues GETs that mutate account state.

#### ADMIN-AUTHZ-005 — No object-level / tenant scoping; any admin acts on any customer
- **Severity:** Medium (by-design, but unbounded blast radius)
- **Files:** `resource/UserResource.java:35-53`, `service/UserServiceImpl/UserServiceImpl.java:102-118`
- **Description:** Every admin endpoint operates globally with no segmentation (branch/region/least
  privilege). A single compromised or rogue admin can read all PII/financials and toggle every
  account. There is no scoping of which customers an admin may service.
- **Exploit scenario:** A low-trust support admin enumerates all users via `/api/user/all` and pulls
  full transaction histories for high-value customers they have no business reason to access.

#### ADMIN-AUTHZ-006 — No audit logging on privileged admin mutations
- **Severity:** Medium
- **Files:** `service/UserServiceImpl/UserServiceImpl.java:102-114`, `service/UserServiceImpl/AppointmentServiceImpl.java:30-34`
- **Description:** enable/disable/confirm produce only `System.out.println` debug lines — no
  structured, attributable audit record (who/when/target). Abuse of admin power is undetectable and
  non-forensic.
- **Exploit scenario:** A rogue admin disables accounts or confirms fraudulent appointments; there
  is no audit trail to attribute or reconstruct the actions.

#### ADMIN-AUTHZ-007 — CORS allows credentialed requests via custom filter despite `cors().disable()`
- **Severity:** Medium
- **Files:** `config/RequestFilter.java:15-44`, `config/SecurityConfig.java:60`
- **Description:** `RequestFilter` runs at `HIGHEST_PRECEDENCE` and unconditionally sets
  `Access-Control-Allow-Origin: http://localhost:4200` with `Access-Control-Allow-Credentials: true`
  for every response, bypassing Spring's disabled CORS. The allowed origin is a hardcoded
  dev origin shipped in source; if reused/edited carelessly (e.g. to a wildcard or attacker origin)
  it directly enables credentialed cross-origin reads of admin data. The filter also swallows all
  exceptions (`e.printStackTrace()`), masking downstream security failures.
- **Exploit scenario:** A future edit relaxes the origin (a common "make CORS work" fix); combined
  with `Allow-Credentials: true`, any site can read `/api/user/all` using the admin's cookie.

### IDOR — Insecure direct object reference

#### ADMIN-IDOR-001 — `username` query param directly dereferences any user's primary transactions
- **Severity:** High
- **Files:** `resource/UserResource.java:35-38`, `service/UserServiceImpl/TransactionServiceImpl.java:47-52`
- **Description:** `GET /api/user/primary/transaction?username=<any>` takes an attacker-controlled
  identifier and returns that account's full ledger with zero ownership/relationship checks beyond
  the coarse ADMIN role. Classic direct object reference by external key.
- **Exploit scenario:** Any admin (or any caller if AUTHZ-001 regresses) iterates usernames from
  `/api/user/all` and harvests every customer's transaction history.

#### ADMIN-IDOR-002 — `username` query param directly dereferences any user's savings transactions
- **Severity:** High
- **Files:** `resource/UserResource.java:40-43`, `service/UserServiceImpl/TransactionServiceImpl.java:54-59`
- **Description:** Same pattern as IDOR-001 for the savings ledger; unbounded object access keyed on
  the supplied `username`.
- **Exploit scenario:** Mass extraction of savings transaction histories for arbitrary users.

#### ADMIN-IDOR-003 — `username` path variable on enable/disable references any account
- **Severity:** High
- **Files:** `resource/UserResource.java:45-53`, `service/UserServiceImpl/UserServiceImpl.java:102-114`
- **Description:** `enableUser`/`disableUser` mutate the account identified solely by the path
  `{username}`, with no check that the target is in scope. Combined with GET + no CSRF (AUTHZ-003/4),
  this is a directly addressable, mutating object reference.
- **Exploit scenario:** `GET /api/user/<victim>/disable` locks out any chosen customer; iterating
  `/api/user/all` allows mass account disablement (denial of banking access).

#### ADMIN-IDOR-004 — `id` path variable confirms any appointment with no existence/ownership guard
- **Severity:** Medium
- **Files:** `resource/AppointmentResource.java:29-32`, `service/UserServiceImpl/AppointmentServiceImpl.java:26-34`
- **Description:** `confirmAppointment(id)` calls `findOne(id)` then dereferences/mutates without a
  null check or scope check. Sequential numeric IDs make enumeration trivial; a non-existent ID
  triggers an NPE (`setConfirmed` on null) → 500 / info leak.
- **Exploit scenario:** An admin (or CSRF'd browser) walks `id=1..N` confirming appointments that
  are not theirs to manage; invalid IDs produce stack traces.

#### ADMIN-IDOR-005 — Enumeration oracle via `/api/user/all` feeding the username-keyed endpoints
- **Severity:** Medium
- **Files:** `resource/UserResource.java:30-43`
- **Description:** `/api/user/all` hands the caller the full set of usernames, which are the exact
  keys required by the IDOR-001/002/003 endpoints. The listing endpoint is a built-in enumeration
  oracle that maximises the blast radius of the direct-object-reference endpoints.
- **Exploit scenario:** One call to `/api/user/all`, then scripted fan-out to every per-user
  endpoint, exfiltrating/mutating the entire customer base.

### PRIVESC — Privilege escalation

#### ADMIN-PRIVESC-001 — Admins can disable/enable other admins (peer-privilege abuse / admin DoS)
- **Severity:** High
- **Files:** `resource/UserResource.java:45-53`, `service/UserServiceImpl/UserServiceImpl.java:102-114`
- **Description:** enable/disable target any account including other ROLE_ADMIN accounts; there is no
  self/peer protection. One admin can disable all other admins, then act unchecked — a
  privilege-consolidation / admin-takeover primitive.
- **Exploit scenario:** A rogue admin disables every other admin account, becoming the sole operator,
  then confirms fraudulent appointments / toggles customer accounts without oversight.

#### ADMIN-PRIVESC-002 — Mass-assignment via `@ModelAttribute User` on public signup binds privileged fields
- **Severity:** High
- **Files:** `controller/HomeController.java:49-71`, `domain/User.java:25-58`
- **Description:** Signup binds the whole `User` JPA entity straight from request params
  (`@ModelAttribute("user") User user`). `User` exposes setters for `userId`, `password`, `enabled`,
  `primaryAccount`, `savingsAccount`, etc. Attacker-supplied form fields (e.g. `enabled=true`,
  `userId=<existing>`) are bound before persistence. While `createUser` hardcodes `ROLE_USER`, the
  unfiltered binding of an entity used by the admin domain (and reused by `userDao.save`) is a
  cross-domain escalation/integrity risk (e.g. overwriting an existing record by id, smuggling
  account references).
- **Exploit scenario:** A crafted signup POST sets fields beyond the intended (firstName, lastName,
  username, email, phone, password) to manipulate persisted user state that admin screens then trust.

#### ADMIN-PRIVESC-003 — Self-service account re-enable via CSRF'd GET (bypasses admin disable)
- **Severity:** Medium
- **Files:** `resource/UserResource.java:45-48`, `config/SecurityConfig.java:60`
- **Description:** Because `enable` is an unauthenticated-from-the-browser-perspective CSRF GET
  (AUTHZ-003/4), a fraudster who controls a page an admin visits can re-enable an account an admin
  previously disabled, effectively overriding an administrative control without holding ADMIN.
- **Exploit scenario:** A fraud account is disabled by an admin; the fraudster lures any admin to a
  page that fires `GET /api/user/fraudacct/enable`, restoring the account.

#### ADMIN-PRIVESC-004 — No step-up / re-authentication for high-impact admin actions
- **Severity:** Low
- **Files:** `config/SecurityConfig.java:59-66`, `resource/UserResource.java:45-53`
- **Description:** `rememberMe()` long-lived sessions grant full admin power with no re-auth or MFA
  for destructive actions (disable account, confirm appointment). A hijacked or persistent session
  yields uninterrupted privileged access.
- **Exploit scenario:** A stolen remember-me cookie lets an attacker perform all admin mutations
  indefinitely without ever re-entering credentials.

### SDE — Sensitive data exposure

#### ADMIN-SDE-001 — `/api/user/all` serializes the BCrypt **password hash** of every user
- **Severity:** Critical
- **Files:** `resource/UserResource.java:30-33`, `domain/User.java:33,132-138`, `domain/User.java:49-58`
- **Description:** The endpoint returns raw `User` entities. The `password` field has a public getter
  and is **not** annotated `@JsonIgnore` (only `appointmentList` and `userRoles` are). Every
  response therefore leaks the stored BCrypt hash for all users, enabling offline cracking and
  greatly aiding credential attacks.
- **Exploit scenario:** Any admin (or any authenticated user if AUTHZ-001 regresses) calls
  `/api/user/all`, captures all `password` hashes, and cracks weak passwords offline.

#### ADMIN-SDE-002 — `/api/user/all` over-exposes full PII and account/financial graph
- **Severity:** High
- **Files:** `resource/UserResource.java:30-33`, `domain/User.java:32-54`
- **Description:** Beyond the hash, the entity serializes `username`, `firstName`, `lastName`,
  `email`, `phone`, plus eagerly/lazily reachable `primaryAccount`/`savingsAccount` (balances,
  account numbers) and `recipientList`. There is no DTO/projection; the API returns the entire
  domain graph for every user in one call.
- **Exploit scenario:** A single request yields a complete, exfiltratable dump of customer PII and
  balances — ideal for fraud, phishing, and account takeover targeting.

#### ADMIN-SDE-003 — Deterministic/static BCrypt salt defeats per-password salting
- **Severity:** High
- **Files:** `config/SecurityConfig.java:31-36`
- **Description:** `new BCryptPasswordEncoder(12, new SecureRandom(SALT.getBytes()))` seeds the RNG
  with a hardcoded constant `"salt"`. Seeding `SecureRandom` deterministically makes generated salts
  predictable/repeated across hashes, undermining BCrypt's per-password salt guarantee and enabling
  precomputation/rainbow-style attacks. The salt is also committed in source.
- **Exploit scenario:** With predictable salts and leaked hashes (SDE-001), an attacker precomputes
  candidate hashes far more efficiently than against properly randomly-salted BCrypt.

#### ADMIN-SDE-004 — Vulnerable `auth0-js ^8.8.0` shipped in admin SPA (known CVEs)
- **Severity:** High
- **Files:** `AdminPortal/package.json:24`
- **Description:** The admin portal depends on `auth0-js ^8.8.0`, a version with known published
  advisories (e.g. ID-token/JWT validation weaknesses fixed in later 9.x releases). Even though the
  current login flow uses cookie/form login, the vulnerable library is bundled and importable, and
  the lockfile-free `^` range can resolve to other affected builds.
- **Exploit scenario:** If/when auth0-js is used for token handling, known validation flaws permit
  token forgery/acceptance; at minimum it is an exploitable dependency present in the admin bundle.

#### ADMIN-SDE-005 — Admin credentials and session sent over plaintext HTTP
- **Severity:** High
- **Files:** `AdminPortal/src/app/login.service.ts:10-23`, `AdminPortal/src/app/user.service.ts:10-32`, `AdminPortal/src/app/appointment.service.ts:10-17`
- **Description:** All SPA calls hardcode `http://localhost:8080` (no TLS) and send credentials/session
  cookie with `withCredentials:true`. In any non-localhost deployment this transmits admin
  credentials and session tokens in cleartext, exposing them to network interception.
- **Exploit scenario:** A network MitM captures the admin's login POST body (username/password) and
  session cookie, then replays full admin access.

#### ADMIN-SDE-006 — Sensitive `username` passed in query string (logged)
- **Severity:** Low
- **Files:** `resource/UserResource.java:35-43`, `AdminPortal/src/app/user.service.ts:15-22`
- **Description:** Transaction lookups put the target `username` in the URL query string, which is
  routinely captured by server access logs, proxies, and browser history — leaking which customers
  an admin inspected and creating a secondary PII trail.
- **Exploit scenario:** Anyone with log access reconstructs which accounts were viewed, aiding
  insider targeting or privacy violations.

#### ADMIN-SDE-007 — Debug output and stack traces leak state/internals
- **Severity:** Low
- **Files:** `service/UserServiceImpl/UserServiceImpl.java:111-113`, `config/RequestFilter.java:32-36`
- **Description:** `System.out.println(user.isEnabled())` / `"<username> is disabled."` and
  `e.printStackTrace()` emit account state and exception internals to stdout/logs instead of
  controlled, sanitized logging — minor information disclosure and noise that can mask real failures.
- **Exploit scenario:** Log readers (or anyone who can surface stdout) learn account-state changes
  and internal error details useful for further attacks.

#### ADMIN-SDE-008 — Client-side-only auth gate (`localStorage` flag, no route guards)
- **Severity:** Low
- **Files:** `AdminPortal/src/app/login/login.component.ts:16-33`, `AdminPortal/src/app/app.routing.ts:13-39`
- **Description:** The SPA tracks "logged in" via a `PortalAdminHasLoggedIn` localStorage string and
  defines no Angular route guards. The gate is purely cosmetic (server still enforces auth), but it
  is trivially forgeable and reflects a missing client authorization model.
- **Exploit scenario:** A user sets `localStorage.PortalAdminHasLoggedIn='true'` to render admin
  views; while data calls still require the cookie, the pattern invites trusting client state.

---

## 3. Domain Rollup

### Candidate counts by class and severity

| Vuln class | Critical | High | Medium | Low | Total |
|---|---|---|---|---|---|
| AUTHZ | 0 | 2 | 5 | 0 | 7 |
| IDOR | 0 | 3 | 2 | 0 | 5 |
| PRIVESC | 0 | 2 | 1 | 1 | 4 |
| SDE | 1 | 4 | 0 | 3 | 8 |
| **Total** | **1** | **11** | **8** | **4** | **24** |

### Highest-priority findings

1. **ADMIN-SDE-001 (Critical)** — `/api/user/all` leaks every user's BCrypt password hash; the
   `password` field is missing `@JsonIgnore`.
2. **ADMIN-AUTHZ-003 (High)** — CSRF disabled + GET-based mutating endpoints + credentialed CORS =
   one-click account enable/disable/confirm against any logged-in admin.
3. **ADMIN-IDOR-001/002/003 (High)** — `username`-keyed transaction reads and account enable/disable
   allow unbounded access to any customer, with `/api/user/all` (IDOR-005) as the enumeration oracle.
4. **ADMIN-AUTHZ-001 (High)** — No URL-level `ROLE_ADMIN` restriction on `/api/**`; admin gating is
   single-layered on class-level `@PreAuthorize` and degrades to "any authenticated user" if method
   security regresses.
5. **ADMIN-PRIVESC-001/002 (High)** — Admins can disable peer admins; public signup mass-assigns the
   full `User` entity.

### Cross-cutting themes

- **Single-layer, role-coarse authorization.** One class annotation guards all admin power; no
  URL-layer backstop, no object/tenant scoping, no audit trail. Any AuthZ regression is catastrophic.
- **CSRF/CORS/HTTP-method hygiene.** `csrf().disable()`, mutating GETs, and a credentialed custom
  CORS filter combine into a strong cross-site abuse surface against authenticated admins.
- **Raw entities as API responses.** Returning JPA `User` entities (no DTO) drives the worst leak
  (password hashes) and broad PII/financial exposure; a projection layer would close SDE-001/002.
- **EOL stack & deps.** Spring Boot 1.5.4 (EOL), Java 8, Angular 4, and `auth0-js ^8.8.0` carry known
  unpatched advisories.

### Recommended remediation order (highest leverage first)

1. Add `@JsonIgnore` to `User.password` (and prefer DTOs/projections for all `/api` responses).
2. Re-enable CSRF (or require POST + CSRF token) and restrict enable/disable/confirm to POST.
3. Add URL-level `antMatchers("/api/**").hasRole("ADMIN")` as defense-in-depth alongside `@PreAuthorize`.
4. Replace the static-seeded `BCryptPasswordEncoder` with the default randomly-salted constructor.
5. Introduce object/tenant scoping + audit logging on privileged mutations; protect peer-admin accounts.
6. Lock down the custom CORS filter (explicit allowlist, no blanket credentialed headers) and serve over TLS.
7. Replace `@ModelAttribute User` signup binding with a constrained DTO; upgrade `auth0-js` and the EOL stack.

---

*Generated by the Hunter Pipeline ADMIN domain scanner. Severities are triage estimates pending
exploit validation; counts reflect over-generated candidates and intentionally include
defense-in-depth observations.*
