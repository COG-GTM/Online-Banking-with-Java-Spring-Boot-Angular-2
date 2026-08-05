---
name: testing-online-banking
description: How to build, run and end-to-end test this Online Banking app (Spring Boot UserFront on :8080 + Angular AdminPortal on :4200 + MySQL). Use when smoke-testing user flows, admin portal flows, CORS/session behaviour, or verifying a framework upgrade (Spring Boot / Java / Angular).
---

# Testing the Online Banking app (UserFront + AdminPortal)

Two independently-served apps share one MySQL database:

| Part | Stack | Port |
|---|---|---|
| `UserFront/` | Spring Boot + Thymeleaf, server-rendered user banking UI **and** the `/api/**` REST API | 8080 |
| `AdminPortal/` | Angular SPA, admin UI that calls `http://localhost:8080/api/**` cross-origin | 4200 |

## Bringing the stack up

### 1. MySQL (required — the app will not start without it)
`UserFront/src/main/resources/application.properties` hard-codes the connection. Do **not** edit it; start a DB that matches it instead:

```bash
docker run -d --name obmysql \
  -e MYSQL_ROOT_PASSWORD=avengers1993 \
  -e MYSQL_DATABASE=onlinebanking \
  -p 3306:3306 mysql:8.0
```

Query it from the host (the `mysql` client inside the container image may not be usable via `docker exec`):
```bash
mysql -h 127.0.0.1 -uroot -pavengers1993 onlinebanking -e "select * from role;"
```

### 2. Seed the `role` table — ALWAYS DO THIS FIRST
`ddl-auto=update` creates the schema but there is **no role seeder**. On a fresh DB the `role` table is empty, `roleDao.findByName("ROLE_USER")` returns null, and **signup silently fails** (no user row, no visible error). Seed before anything else:

```bash
mysql -h 127.0.0.1 -uroot -pavengers1993 onlinebanking \
  -e "insert ignore into role(role_id,name) values (1,'ROLE_USER'),(2,'ROLE_ADMIN');"
```

### 3. Backend
Build and run with a matching JDK (check `UserFront/pom.xml` for the target release — a version mismatch shows up as `UnsupportedClassVersionError ... class file version 65.0`, i.e. the jar is Java 21 but you launched an older JVM):

```bash
cd UserFront
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn clean package -DskipTests
# run with the SAME JDK, not whatever `java` resolves to:
setsid nohup /usr/lib/jvm/java-21-openjdk-amd64/bin/java -jar target/userFront-0.0.1-SNAPSHOT.jar \
  > /tmp/backend.log 2>&1 < /dev/null &
```
If Maven Central rate-limits (HTTP 429), configure a mirror in `~/.m2/settings.xml`.

### 4. Frontend
```bash
cd AdminPortal && source ~/.nvm/nvm.sh && nvm use 20 && npm install && npm start   # :4200
```

## Accounts and roles
- Signup **always** grants `ROLE_USER` (`HomeController.signupPost`). There is no UI to create an admin.
- `/api/**` is guarded by `@PreAuthorize("hasRole('ADMIN')")` (`UserResource`, `AppointmentResource`), so the admin portal is useless without an admin. Promote a user directly in the DB:
```bash
mysql -h 127.0.0.1 -uroot -pavengers1993 onlinebanking \
  -e "update user_role set role_id=2 where user_id=(select user_id from user where username='adminuser');"
```
- The admin portal has no separate login backend: `LoginService` POSTs form-encoded credentials to `http://localhost:8080/index` with `withCredentials: true`, then relies on the `JSESSIONID` cookie for `/api/**`.

## UI paths worth knowing
- User app: `/signup` → `/index` (login) → `/userFront`. Navbar: Accounts ▸ Primary/Savings, Transfer ▸ Between Accounts, Appointment ▸ Schedule an Appointment, Me ▸ Profile/Logout.
- Deposit `/account/deposit` and transfer `/transfer/betweenAccounts` both redirect back to `/userFront`, so balances on the home page are the assertion target. Balances start at `0.00`.
- The appointment form uses a legacy jQuery `bootstrap-datetimepicker` on a readonly input: click the field, then drill **month grid → hour list → minute list**; the input then fills with `yyyy-MM-dd HH:mm`. Submitting opens a Bootstrap confirm modal — click **Confirm**.
- Admin portal routes: `/login`, `/userAccount`, `/primaryTransaction/:username`, `/savingsTransaction/:username`, `/appointment`.

## Gotchas that will waste your time
- **A bare `200` with an empty body means a swallowed server exception.** `RequestFilter.doFilter` wraps `chain.doFilter` in `try { ... } catch(Exception e) { e.printStackTrace(); }`, so any exception in the filter chain (security config, matchers, etc.) is returned to the client as an empty `200` instead of a `500`. **Always check `/tmp/backend.log` for a stack trace before believing an empty page.**
- **Spring Security 6 rejects some Ant patterns.** `requestMatchers(String...)` resolves to `MvcRequestMatcher`/`PathPattern`, where `**` must be the last path element. A pattern like `/error/**/*` throws `PatternParseException: No more pattern data allowed after {*...} or ** pattern element` on *every* request. Combined with the swallow above, the whole app looks like it returns blank 200s while booting "successfully". If most pages are blank but `/index` works, suspect `SecurityConfig.PUBLIC_MATCHERS`.
- **The admin portal's logout is racy.** `NavbarComponent.logout()` calls `location.reload()` before the async subscribe clears `localStorage.PortalAdminHasLoggedIn`, so the login form can stay hidden behind "Welcome to Admin Portal!" even though the backend session is gone. To log in as a different user, **use an incognito window** (fresh cookies + localStorage) rather than fighting the flag.
- Hibernate logs `HHH90000025: MySQLDialect does not need to be specified explicitly` — cosmetic.
- Thymeleaf logs `Deprecated unwrapped fragment expression` — cosmetic.

## Suggested smoke test (proves both apps and the cross-origin link)
1. Sign up a user in the UI → log in → assert `/userFront` shows `0.00` / `0.00`.
2. Deposit `500` to Primary → assert `500.00`; transfer `200` Primary→Savings → assert `300.00` / `200.00`.
3. Open `/account/primaryAccount` → assert exactly 2 reconciling rows (500 → bal 500, 200 → bal 300).
4. Schedule an appointment; log out → assert `/index?logout` and that `/userFront` redirects back to login.
5. In the admin portal, log in as the admin → User Account → assert the new user's row shows Primary `300` / Savings `200` (**the same values created on the other origin — this is what actually proves CORS + session, not merely that the page renders**).
6. Disable/enable the user, open the primary transaction list, confirm the appointment.
7. **Negative test:** in an incognito window, log into the admin portal as a `ROLE_USER` account and open User Account → `/api/user/all` must return **403** and the table must be empty. This is the check that catches an authorization config that has collapsed into permit-all.
8. For CORS evidence, inspect the `/api/user/all` request in DevTools → Network: expect `Access-Control-Allow-Origin: http://localhost:4200`, `Access-Control-Allow-Credentials: true` on the response and `Cookie: JSESSIONID=...` on the request.

## Devin Secrets Needed
None. All credentials are local/hard-coded in `application.properties` (`root` / `avengers1993`).
