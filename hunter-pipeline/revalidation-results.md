# Hunter Pipeline — Post-Fix Revalidation Results

**Repo:** COG-GTM/Online-Banking-with-Java-Spring-Boot-Angular-2
**Stack:** Java 8, Spring Boot 1.5.4, Spring Security 4.2, Thymeleaf, MySQL
**Fix branch revalidated:** `devin/1780594490-security-remediation` (PR #26, head `f7e2de8`)
**Base:** `master` (`260093a`)
**Revalidation date:** 2026-06-04

## Method
- **Static re-trace:** Read the fixed code on the remediation branch and confirmed each vulnerable code path is now blocked.
- **Dynamic re-test:** Built the app with JDK 8 (`cd UserFront && mvn clean package -DskipTests` → BUILD SUCCESS, `target/userFront-0.0.1-SNAPSHOT.jar`), ran it against a local MariaDB `onlinebanking` DB started with externalized credentials (`DB_USERNAME` / `DB_PASSWORD`), seeded roles, and registered three users (`attacker`, `victim`, `admin1`→`ROLE_ADMIN`). Then re-sent the exploit requests with authenticated sessions and CSRF tokens and observed HTTP responses + DB state.

> Note: the built artifact is `userFront-0.0.1-SNAPSHOT.jar` (lowercase `u`), not `UserFront-...` as written in the task. Build/run commands otherwise match.

## Summary Table

| # | Fix | Classification | Static | Dynamic |
|---|-----|----------------|--------|---------|
| 1 | `@JsonIgnore` on `User.password` — `/api/user/all` no longer leaks password | **fix_verified** | ✅ | ✅ no `password` in JSON |
| 2 | Amount validation — reject negative/zero (and over-balance) on deposit/withdraw/transfer | **fix_verified** | ✅ | ✅ all rejected, balance only moved on valid amount |
| 3 | CSRF re-enabled for non-API POSTs | **fix_verified** | ✅ | ✅ token-less POST → 403 |
| 4 | Profile IDOR — `profilePost` uses `Principal`, not request username | **fix_verified** | ✅ | ✅ victim untouched, change applied to self |
| 5 | Recipient IDOR — `find/deleteRecipientByName` user-scoped | **fix_verified** | ✅ | ✅ cross-user view/delete blocked |
| 6 | DB credentials externalized to `${DB_USERNAME}` / `${DB_PASSWORD}` | **fix_verified** | ✅ | ✅ app boots on env creds |
| 7 | Admin `username` parameter validation in `UserResource` | **fix_verified** | ✅ | ✅ unknown/blank → 400, valid → 200 |
| 8 | Recipient delete + appointment confirm now POST-only | **fix_verified** | ✅ | ✅ GET → 405 |
| 9 | Appointment mass-assignment — individual `@RequestParam` | **fix_verified** | ✅ | ✅ `confirmed`/`id`/`user` not injectable |
| 10 | CORS exact-origin match in `RequestFilter` | **fix_verified** | ✅ | ✅ evil origin gets no ACAO; trusted origin allowed |

**Result: 10 / 10 fix_verified.** No `fix_partial`, `fix_ineffective`, or `retest_blocked`.

A few non-blocking residual observations are listed at the end; none change the classifications above.

---

## Detailed Findings

### 1. Password hash leak — fix_verified
**Static:** `User.password` now carries `@JsonIgnore` (`domain/User.java:34`) and is removed from `toString()`. `getPassword()` is un-annotated, so Jackson suppresses the whole logical property. `UserResource.userList()` (`/api/user/all`) still returns `List<User>`, but the password is no longer serialized.

**Dynamic:** `GET /api/user/all` as `admin1` → HTTP 200. Response contained `username`, account data, `authorities`, etc. but **no `password` field**:
```
[{"userId":1,"username":"attacker",...,"authorities":[{"authority":"ROLE_USER"}],...}]
```
Grep for `"password"` in the response: no match.

### 2. Negative / zero / over-balance amounts — fix_verified
**Static:** Service-layer guards reject `amount <= 0` in `AccountServiceImpl.deposit/withdraw` and `TransactionServiceImpl.betweenAccountsTransfer/toSomeoneElseTransfer`, plus insufficient-funds checks (`compareTo(balance) < 0`). Controllers (`AccountController`, `TransferController`) add fast-fail guards that redirect back on non-positive amounts.

**Dynamic (attacker, Primary account, start balance 0.00):**
| Request | HTTP | Effect |
|---|---|---|
| `deposit amount=-100` | 302 → `/account/deposit` | rejected |
| `deposit amount=0` | 302 → `/account/deposit` | rejected |
| `withdraw amount=-50` | 302 → `/account/withdraw` | rejected |
| `deposit amount=250` (control) | 302 → `/userFront` | applied |

Final DB balance = **250.00** — i.e. only the single valid deposit moved funds; every negative/zero attempt was a no-op.

### 3. CSRF protection — fix_verified
**Static:** `SecurityConfig` now uses `.csrf().ignoringAntMatchers("/api/**")` (CSRF enabled everywhere except the cross-origin Angular `/api/**` surface). Thymeleaf `th:action` forms auto-inject the `_csrf` token.

**Dynamic:** Authenticated `POST /account/deposit` **without** a `_csrf` token → **HTTP 403**, balance unchanged. With a valid token the same request succeeds (see Fix 2), confirming the token is what is enforced.

### 4. Profile IDOR — fix_verified
**Static:** `profilePost` now loads the user via `userService.findByUsername(principal.getName())` and no longer calls `setUsername(...)`, so a request-supplied `username` cannot retarget another account.

**Dynamic:** Logged in as `attacker`, posted `/user/profile` with `username=victim&firstName=HACKED`. Result: `victim.first_name` stayed `victim` (untouched); `attacker.first_name` became `HACKED`. The edit applied only to the authenticated principal.

### 5. Recipient IDOR — fix_verified
**Static:** `findRecipientByName` / `deleteRecipientByName` now take a `Principal` and only return/delete a recipient whose `recipient.getUser().getUsername()` equals the caller. Controllers pass `Principal` and redirect on a `null` (non-owned) result.

**Dynamic:** `victim` created recipient `VictimRecipient`. As `attacker`:
- `GET /transfer/recipient/edit?recipientName=VictimRecipient` → 302 redirect to `/transfer/recipient` (no data disclosed).
- `POST /transfer/recipient/delete?recipientName=VictimRecipient` (valid CSRF) → 200, but DB still shows the recipient (`COUNT=1`). Cross-user delete was a no-op.

### 6. Externalized DB credentials — fix_verified
**Static:** `application.properties` now reads `spring.datasource.username = ${DB_USERNAME:root}` and `spring.datasource.password = ${DB_PASSWORD:}`. No plaintext credentials remain in the file.

**Dynamic:** The app booted and served all traffic using `DB_USERNAME=bankuser` / `DB_PASSWORD=bankpass` supplied purely via environment variables.

> Follow-up (carried over from remediation report): the previously committed password (`avengers1993`) still exists in git history and should be rotated.

### 7. Admin parameter validation — fix_verified
**Static:** `UserResource` retains the class-level `@PreAuthorize("hasRole('ADMIN')")` and adds `validateUsername(...)` (invoked by transaction-list, enable, disable), throwing a `@ResponseStatus(BAD_REQUEST)` exception for blank/unknown usernames.

**Dynamic (as `admin1`):**
| Request | HTTP |
|---|---|
| `GET /api/user/doesnotexist999/enable` | 400 |
| `GET /api/user/primary/transaction?username=` (blank) | 400 |
| `GET /api/user/primary/transaction?username=attacker` (valid) | 200 |

> Scope note: this is fundamentally **input validation / NPE-prevention**; the actual authorization (an admin may act on any user) is the retained `@PreAuthorize`. The vulnerable behavior described (NPE / unvalidated input) is blocked.

### 8. State-changing GET → POST — fix_verified
**Static:** `TransferController.recipientDelete` is now `method = POST`, `recipient.html` uses an inline POST form (CSRF auto-injected), `AppointmentResource.confirmAppointment` is `method = POST`, and the Angular `confirmAppointment` uses `http.post`.

**Dynamic:**
| Request | HTTP |
|---|---|
| `GET /transfer/recipient/delete?recipientName=x` | 405 |
| `GET /api/appointment/1/confirm` | 405 |

### 9. Appointment mass-assignment — fix_verified
**Static:** `createAppointmentPost` binds only `location`, `description`, `dateString` via `@RequestParam`, constructs a fresh `Appointment`, forces `confirmed=false`, and sets `user` from the principal — `id`/`confirmed`/`user` can no longer be supplied by the client.

**Dynamic:** As `attacker`, posted `/appointment/create` with extra `confirmed=true&id=999`. Created row: `id=1` (auto-generated, not 999), `confirmed=0`, `owner=attacker`. Injection ignored.

### 10. CORS exact-origin match — fix_verified
**Static:** `RequestFilter` compares the request `Origin` against `ALLOWED_ORIGIN` (`http://localhost:4200`) and only emits `Access-Control-Allow-Origin` + `Access-Control-Allow-Credentials: true` (+ `Vary: Origin`) on an exact match.

**Dynamic:**
- `OPTIONS /api/user/all` with `Origin: http://evil.com` → 200 with **no** `Access-Control-Allow-Origin` header (browser would block).
- `OPTIONS /api/user/all` with `Origin: http://localhost:4200` → 200 with `Access-Control-Allow-Origin: http://localhost:4200` and `Access-Control-Allow-Credentials: true`.

---

## Residual Observations (non-blocking; do not change classifications)
- **#1:** `/api/user/all` still serializes account numbers, balances, and authorities. Password (the sensitive field flagged) is gone and there is no `ssn` field in the model. If broader data minimization is desired, a dedicated DTO would be the next step.
- **#7 / #8:** The admin endpoints `/api/user/{username}/enable` and `/disable` are still mapped without an explicit HTTP method, so they remain reachable via GET (they are state-changing). This is outside the listed scope of fix #8 (which covered recipient delete and appointment confirm) but is worth a follow-up — convert them to POST. They remain protected by `@PreAuthorize("hasRole('ADMIN')")` and, being under `/api/**`, are CSRF-exempt by design for the Angular portal.
- **#10:** `Access-Control-Allow-Methods/Headers/Max-Age` are emitted unconditionally, but without `Access-Control-Allow-Origin` they are inert for disallowed origins.

## Reproduction
Build: `cd UserFront && JAVA_HOME=<jdk8> mvn clean package -DskipTests`
Run: `DB_USERNAME=bankuser DB_PASSWORD=bankpass java -jar target/userFront-0.0.1-SNAPSHOT.jar` (against a MySQL/MariaDB `onlinebanking` DB with the `role` table seeded with `ROLE_USER`/`ROLE_ADMIN`).
