# Compiled Vulnerability Candidates — Online Banking (Java Spring Boot + Angular 2)

**Pipeline Stage:** 2 (Domain Scanning) → Compiled  
**Total Candidates:** 131  
**Scanned Domains:** AUTH (18), ACCT (14), XFER (22), APPT (11), ADMIN (24), CONFIG (19), DEPS (23)

## Severity Summary

| Severity | Count |
|----------|-------|
| Critical | 16 |
| High | 46 |
| Medium | 39 |
| Low | 17 |
| Info | 1 |

---

## AUTH Domain — 18 candidates (Critical 1, High 6, Medium 7, Low 4)

Vuln classes: AUTHZ (5), CRYPTOFAIL (4), MISCONFIG (5), SDE (4)

### Critical
- **AUTH-CRYPTOFAIL-001:** Static BCrypt salt — `SecureRandom(SALT.getBytes())` seeded with constant `"salt"` makes all password hashes deterministic. (`SecurityConfig.java:31-36`)

### High
- **AUTH-AUTHZ-001:** IDOR — `profilePost` loads target by request-supplied username instead of `Principal`; any user can edit any other account. (`UserController.java:31-45`)
- Mass-assignment on signup, unauth `/console/**`, global CSRF-off, EOL Spring Boot 1.5.4, hardcoded DB creds.

---

## ACCT Domain — 14 candidates (Critical 2, High 4, Medium 6, Low 3)

Vuln classes: AUTHZ (7), IDOR (4), INJ (4)

### Critical
- **ACCT-INJ-001 (conditional):** Arbitrary SQL via H2 console (if console servlet is enabled in application.properties)

### High
- Recipient by-name read/delete with no ownership scoping (IDOR)
- Public H2 console (`SecurityConfig.java:47`), unbounded self-credit on deposit, overdraft/negative balance on withdraw

### Notes
- All persistence is Spring Data derived queries — no classic SQL-injection surface. INJ findings are numeric-parsing / second-order / DB-console.

---

## XFER Domain — 22 candidates (Critical 2, High 12, Medium 5, Low 2, Info 1)

Vuln classes: AUTHZ (6), IDOR (5), INJ (5), CSRF (6)

### Critical
- **XFER-INJ-002:** No sign/balance validation; negative `amount` on `/transfer/toSomeoneElse` credits caller's own account. (`TransactionServiceImpl.java:124-142`)
- **XFER-CSRF-004:** CSRF globally disabled → forged transfer to attacker-planted recipient. (`SecurityConfig.java:60`)

### High
- Global unscoped `findByName`/`deleteByName` (`RecipientDao.java:12,14`) — cross-user recipient read/delete + PII/account-number disclosure
- GET used for state-changing delete operations

### Confirmed Negatives
- SQLi not reachable (parameterized Spring Data queries)
- Stored XSS not reachable (Thymeleaf auto-escaping, no `th:utext`)

---

## APPT Domain — 11 candidates (High 2, Medium 5, Low 4)

Vuln classes: IDOR (7), INJ (4)

### High
- **APPT-IDOR-001:** `POST /appointment/create` binds whole `Appointment` entity incl. `id`; `save()` performs update → any authenticated user can overwrite another's appointment. (`AppointmentController.java:39-53`)
- **APPT-IDOR-004:** `/api/appointment/all` serializes raw `User`; `User.password` is not `@JsonIgnore`'d → leaks all customers' bcrypt hashes + PII.

### Medium
- Mass-assigning `confirmed=true` self-confirms, bypassing admin-only approval flow
- State-changing confirm GET + globally disabled CSRF

### Notes
- INJ class mostly clean for SQLi (parameterized Spring Data only); remaining INJ items are latent stored-XSS (no live unsafe sink) and lenient date-parse defect.

---

## ADMIN Domain — 24 candidates (Critical 1, High 11, Medium 8, Low 4)

Vuln classes: AUTHZ (7), IDOR (5), PRIVESC (4), SDE (8)

### Critical
- **ADMIN-SDE-001:** `/api/user/all` returns raw `User` entities; `User.password` missing `@JsonIgnore` → leaks every user's BCrypt hash.

### High
- **ADMIN-AUTHZ-003:** `csrf().disable()` + mutating endpoints reachable via GET + credentialed CORS → one-click cross-site enable/disable/confirm
- **ADMIN-IDOR-001/002/003:** `username`-keyed transaction reads and enable/disable reach any customer; `/api/user/all` is the enumeration oracle
- **ADMIN-AUTHZ-001:** No URL-level `hasRole('ADMIN')` on `/api/**`; admin gating is single-layered on class-level `@PreAuthorize`
- **ADMIN-PRIVESC-001/002:** Admins can disable peer admins; public signup mass-assigns full `User` entity via `@ModelAttribute`

---

## CONFIG Domain — 19 candidates (Critical 4, High 5, Medium 6, Low 3, Info 1)

Vuln classes: MISCONFIG (7), CRYPTOFAIL (6), SDE (6)

### Critical
- **CONFIG-CRYPTOFAIL-001:** Static `SecureRandom("salt")` defeating BCrypt salts
- **CONFIG-MISCONFIG-003:** Public `/console/**` DB console
- **CONFIG-CRYPTOFAIL-003:** Plaintext DB credentials (`root`/`avengers1993`)
- **CONFIG-SDE-001:** Credentials committed to git

### High
- Global CSRF disable + permissive manual CORS
- Spring Boot 1.5.4 EOL with known CVEs

---

## DEPS Domain — 23 candidates (Critical 6, High 8, Medium 7, Low 1)

Vuln class: DEPS (23)

### Critical
- **DEPS-DEPS-001:** jackson-databind 2.8.8 — deserialization RCE cluster
- **DEPS-DEPS-002:** spring-framework 4.3.9 — SpEL RCE (CVE-2018-1270) + Spring4Shell adjacency
- **DEPS-DEPS-003:** spring-data-commons 1.13.4 — SpEL injection RCE (CVE-2018-1273, CVSS 9.8)
- **DEPS-DEPS-004:** tomcat-embed 8.5.15 — Ghostcat (CVE-2020-1938) + RCE cluster
- **DEPS-DEPS-005:** snakeyaml 1.17 — deserialization RCE (CVE-2022-1471)
- **DEPS-DEPS-016:** auth0-js ^8.8.0 — JWT validation bypass (CVE-2020-15084, CVSS 9.8)

### Remediation Lever
Migrating UserFront to Spring Boot 3.x transitively retires ~11 backend findings.

---

## Cross-Domain Attack Chains (Preliminary)

1. **Full Account Takeover:** AUTH-AUTHZ-001 (profile edit IDOR) → CONFIG-CRYPTOFAIL-001 (static salt precomputation) → ADMIN-SDE-001 (hash leak from `/api/user/all`)
2. **Fraudulent Transfer:** XFER-INJ-002 (negative amount credit) → XFER-CSRF-004 (forged transfer) → ACCT deposits into attacker account
3. **Privilege Escalation:** ADMIN-PRIVESC-002 (mass-assignment on signup with role injection) → ADMIN-IDOR-001 (view all transactions) → ADMIN-SDE-001 (leak all hashes)
4. **Database Compromise:** CONFIG-MISCONFIG-003 (public H2 console) → CONFIG-CRYPTOFAIL-003 (hardcoded DB creds) → full DB read/write access
5. **Lateral Movement:** DEPS-DEPS-001 (jackson-databind RCE) or DEPS-DEPS-003 (spring-data-commons RCE) → OS-level code execution → full system compromise
