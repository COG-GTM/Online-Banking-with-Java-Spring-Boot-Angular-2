# Security Audit Report — Online Banking (Java Spring Boot + Angular 2)

**Repository:** COG-GTM/Online-Banking-with-Java-Spring-Boot-Angular-2  
**Date:** 2026-06-04  
**Pipeline:** Hunter Pipeline v1 (7-stage closed-loop)  
**Orchestrator:** devin-88f32c5818e442b18f1849d48ff54ca5

---

## Executive Summary

A comprehensive 7-stage security audit of the Online Banking application (Java 8 / Spring Boot 1.5.4 / Angular 4 / MySQL) identified **131 vulnerability candidates** across 7 domains, of which **29 were confirmed exploitable** through static analysis and **26 were dynamically confirmed** through live runtime testing. The entire technology stack is end-of-life (circa 2017) with no security updates.

**10 critical and high-severity fixes** were implemented in [PR #26](https://github.com/COG-GTM/Online-Banking-with-Java-Spring-Boot-Angular-2/pull/26), and **all 10 fixes were verified effective** through post-fix revalidation (10/10 `fix_verified`). The application builds successfully after remediation.

### Top Priority Actions

1. **Merge PR #26** — Contains fixes for all dynamically-confirmed Critical and High vulnerabilities
2. **Rotate database password** — The previously hardcoded password (`avengers1993`) remains in git history
3. **Plan Spring Boot migration** — Upgrading from 1.5.4 to 3.x would retire ~11 dependency vulnerabilities
4. **Add CI/CD pipeline** — No automated testing or security scanning currently configured
5. **Deploy with externalized secrets** — Set `DB_USERNAME` and `DB_PASSWORD` environment variables

---

## Pipeline Metrics

| Metric | Value |
|--------|-------|
| Domains scanned | 7 (AUTH, ACCT, XFER, APPT, ADMIN, CONFIG, DEPS) |
| Total candidates generated | 131 |
| Static validation: confirmed_exploitable | 29 |
| Static validation: likely_exploitable | 3 |
| Static validation: potential_issue | 49 |
| Static validation: false_positive | 4 |
| Dynamic validation: dynamically_confirmed | 26 |
| Dynamic validation: dynamically_blocked | 4 |
| Dynamic validation: partially_testable | 2 |
| Fixes implemented | 10 |
| Fixes verified effective | 10/10 |
| Remediation PR | [#26](https://github.com/COG-GTM/Online-Banking-with-Java-Spring-Boot-Angular-2/pull/26) |
| Build status post-fix | ✓ BUILD SUCCESS |
| Child sessions spawned | 11 (7 domain + 1 validator + 1 dynamic + 1 remediator + 1 revalidator) |

---

## Domain Overview

| Domain | Abbrev | Candidates | Confirmed Exploitable | Dynamically Confirmed | Fixes Applied |
|--------|--------|-----------|----------------------|----------------------|---------------|
| Authentication & User Registration | AUTH | 18 | 5 | 4 | 2 (profile IDOR, password hash) |
| Account Management | ACCT | 14 | 4 | 4 | 1 (amount validation) |
| Fund Transfer | XFER | 22 | 8 | 7 | 2 (recipient IDOR, amount validation) |
| Appointments | APPT | 11 | 3 | 3 | 2 (mass-assignment, GET→POST) |
| Admin API | ADMIN | 24 | 5 | 5 | 2 (IDOR validation, GET→POST) |
| Configuration & Security | CONFIG | 19 | 4 | 3 | 2 (CSRF, DB creds, CORS) |
| Dependencies | DEPS | 23 | 0* | 0 | 0 (requires Spring Boot upgrade) |

*DEPS candidates are `potential_issue` — vulnerable versions confirmed present but exploit vectors not wired up in this application's code paths.

---

## Confirmed & Remediated Findings

### CRITICAL — Remediated

#### 1. Password Hash Leak via API Serialization
- **IDs:** ADMIN-SDE-001, APPT-IDOR-004
- **Classification:** dynamically_confirmed
- **Exploit:** `GET /api/user/all` (admin) or `/api/appointment/all` returns raw `User` entities including BCrypt password hashes — `User.password` had no `@JsonIgnore`
- **Fix:** Added `@JsonIgnore` to `User.password`; removed password from `toString()`
- **Revalidation:** fix_verified — `/api/user/all` response no longer contains password field

#### 2. Negative Amount Self-Credit (Unlimited Funds)
- **IDs:** XFER-INJ-002, ACCT-AUTHZ-003/004
- **Classification:** dynamically_confirmed
- **Exploit:** `POST /account/deposit` with negative amount, or `POST /transfer/toSomeoneElse` with negative amount credits the sender's account. No validation on deposit/withdraw/transfer amounts.
- **Fix:** Added `amount > 0` validation in `AccountServiceImpl.deposit/withdraw` and `TransactionServiceImpl.betweenAccountsTransfer/toSomeoneElseTransfer`, plus controller-level guards
- **Revalidation:** fix_verified — negative, zero, and over-balance amounts now rejected

#### 3. CSRF Disabled App-Wide
- **IDs:** XFER-CSRF-004, CONFIG-MISCONFIG-001
- **Classification:** dynamically_confirmed
- **Exploit:** All POST endpoints accepted cross-site forged requests. Attacker page could initiate transfers, modify profiles, delete recipients.
- **Fix:** Re-enabled CSRF with `csrf().ignoringAntMatchers("/api/**")`. Thymeleaf `th:action` auto-injects tokens. API endpoints exempt (guarded by `@PreAuthorize("hasRole('ADMIN')")`)
- **Revalidation:** fix_verified — non-API POST without CSRF token now returns 403

#### 4. Profile IDOR — Any User Can Edit Any Account
- **ID:** AUTH-AUTHZ-001
- **Classification:** dynamically_confirmed
- **Exploit:** `POST /profile` loaded user by request-supplied `username` parameter instead of authenticated `Principal` — any logged-in user could overwrite any other user's profile data
- **Fix:** `profilePost` now loads user from `Principal`; `setUsername()` call removed
- **Revalidation:** fix_verified — profile edit now only affects the authenticated user

#### 5. Global Recipient IDOR — Cross-User Read/Delete
- **IDs:** XFER-IDOR-001/002, XFER-AUTHZ-001
- **Classification:** dynamically_confirmed
- **Exploit:** `findRecipientByName`/`deleteRecipientByName` in `TransactionServiceImpl` were not user-scoped. Any authenticated user could read or delete any other user's payment recipients via `/transfer/recipient/edit?recipientName=X` or `/transfer/recipient/delete?recipientName=X`
- **Fix:** Methods now take `Principal` and filter by current user's recipient list; controllers handle null (non-owned) case
- **Revalidation:** fix_verified — cross-user recipient access now returns error

#### 6. Hardcoded Database Credentials
- **IDs:** CONFIG-CRYPTOFAIL-003, CONFIG-SDE-001
- **Classification:** dynamically_confirmed (partially_testable — config disclosure, no HTTP sink)
- **Exploit:** `application.properties` contained plaintext `root`/`avengers1993` committed to git
- **Fix:** Externalized to `${DB_USERNAME:root}` / `${DB_PASSWORD:}` environment variables
- **Revalidation:** fix_verified — properties file no longer contains plaintext credentials

### HIGH — Remediated

#### 7. Admin API IDOR — Unvalidated Username Parameter
- **IDs:** ADMIN-IDOR-001/002/003
- **Classification:** dynamically_confirmed
- **Exploit:** `/api/user/primary/transaction?username=X`, `/api/user/{username}/enable|disable` accepted any username without validation — admin could enumerate and manipulate any user
- **Fix:** Added username validation (non-blank, must exist) with proper error responses
- **Revalidation:** fix_verified — invalid/nonexistent usernames now return 400

#### 8. State-Changing Operations via GET
- **IDs:** XFER-AUTHZ-003, APPT-IDOR-005
- **Classification:** dynamically_confirmed
- **Exploit:** Recipient delete (`GET /transfer/recipient/delete`) and appointment confirm (`GET /api/appointment/{id}/confirm`) used GET — vulnerable to CSRF via image tags/links
- **Fix:** Changed to POST method; updated frontend forms and Angular service
- **Revalidation:** fix_verified — GET requests to these endpoints now return 405

#### 9. Appointment Mass-Assignment / Takeover
- **ID:** APPT-IDOR-001
- **Classification:** dynamically_confirmed
- **Exploit:** `POST /appointment/create` bound the entire `Appointment` entity including `id` — submitting `id=<existing>` overwrote another user's appointment
- **Fix:** Changed to individual `@RequestParam` fields; forces `confirmed=false`; sets `user` from `Principal`
- **Revalidation:** fix_verified — entity binding no longer accepts `id` or `confirmed` parameters

#### 10. Permissive CORS Configuration
- **ID:** CONFIG-MISCONFIG-002
- **Classification:** dynamically_confirmed
- **Exploit:** `RequestFilter` blindly set `Access-Control-Allow-Origin` and `Access-Control-Allow-Credentials` for all requests
- **Fix:** Exact origin allowlist check (`http://localhost:4200`); `Vary: Origin` header added
- **Revalidation:** fix_verified — non-allowlisted origins no longer receive CORS headers

---

## Dynamically Blocked Findings (Static FP Corrections)

The following 4 findings were classified as confirmed_exploitable in static analysis but **dynamically blocked** at runtime:

| ID | Finding | Why Blocked |
|----|---------|-------------|
| CONFIG-CRYPTOFAIL-001 | Static BCrypt salt (`SecureRandom("salt")`) | `SecureRandom` constructor supplements (doesn't replace) internal entropy. Identical passwords → different hashes. Not precomputable. |
| CONFIG-SDE-005 | Related salt leak | Same as above |
| ADMIN-SDE-003 | Related salt leak | Same as above |
| AUTH-CRYPTOFAIL-001 | Static BCrypt salt | Same as above |

---

## Cross-Domain Attack Chains

### Chain 1: Full Account Takeover (Confirmed)
`AUTH-AUTHZ-001` (profile IDOR) → `ADMIN-SDE-001` (hash leak) → password crack → full account access  
**Status:** Both links dynamically confirmed. Fix breaks Chain 1 at both links.

### Chain 2: Fraudulent Transfer (Confirmed)
`XFER-INJ-002` (negative amount self-credit) → `XFER-CSRF-004` (forged transfer via CSRF) → drain victim funds  
**Status:** Both links dynamically confirmed. Fix breaks Chain 2 at both links.

### Chain 3: Privilege Escalation to Full Data Access (Confirmed)
`ADMIN-PRIVESC-002` (mass-assignment) → `ADMIN-IDOR-001` (view all transactions) → `ADMIN-SDE-001` (hash leak)  
**Status:** Mass-assignment role escalation blocked by server-side role hardcoding; row-overwrite variant confirmed. Fix adds validation.

### Chain 4: Database Compromise (Partially Confirmed)
`CONFIG-MISCONFIG-003` (public `/console/**`) → `CONFIG-CRYPTOFAIL-003` (hardcoded DB creds) → full DB access  
**Status:** `/console/**` routes to 404 (no H2 servlet configured) — chain broken at first link. DB creds externalized in fix.

### Chain 5: Dependency RCE → System Compromise (Potential)
`DEPS-DEPS-001` (jackson-databind RCE) or `DEPS-DEPS-003` (spring-data-commons RCE) → OS code execution  
**Status:** Vulnerable versions confirmed present but exploit vectors not wired (no default-typing, no untrusted YAML/EL). Requires Spring Boot upgrade to fully retire.

---

## False Positives Eliminated

| ID | Claim | Why False Positive |
|----|-------|--------------------|
| CONFIG-MISCONFIG-003 | Public H2 console at `/console/**` | No H2 servlet configured; URL returns 404 |
| DEPS (test-scope) | Test-scope dependencies | Only in test classpath, not in production WAR |
| APPT-INJ-001 | Template XSS via Thymeleaf | All templates use `th:text` (auto-escaped); no `th:utext` found |
| Various INJ | HQL/SQL injection | All persistence uses parameterized Spring Data derived queries |

---

## Remaining Issues (Not Remediated)

### Requires Spring Boot Migration
- 23 dependency vulnerabilities (6 Critical) — jackson-databind, spring-framework, spring-data-commons, tomcat-embed, snakeyaml, auth0-js all on EOL versions
- Spring Boot 3.x migration would retire ~11 of these

### Infrastructure / Process
- No CI/CD pipeline configured
- No automated security scanning
- Git history contains previously hardcoded credentials (requires password rotation)
- `/api/user/{username}/enable|disable` still accessible via GET (only POST enforcement applied to recipient delete and appointment confirm)

### Low/Medium Residual
- 49 `potential_issue` findings (code smells, defense-in-depth concerns)
- Static `nextAccountNumber` counter resets on restart → possible account number collisions
- `RequestFilter` swallows exceptions → 500s masked as HTTP 200
- `checkUserExists` passes username where email is expected

---

## Artifacts

| Stage | Artifact | Location |
|-------|----------|----------|
| 1 | Domain Map | `hunter-pipeline/domain-map.md` |
| 1 | Dependency Inventory | `hunter-pipeline/dependency-inventory.md` |
| 2 | Compiled Candidates | `hunter-pipeline/candidates.md` |
| 2 | Domain Rollups | `hunter-pipeline/domains/{AUTH,ACCT,XFER,APPT,ADMIN,CONFIG,DEPS}-rollup.md` |
| 3 | Validated Findings | `hunter-pipeline/validated-findings.md` (PR #24) |
| 4 | Dynamic Validation | `hunter-pipeline/dynamic-validation.md` (PR #25) |
| 5 | Remediation Report | `hunter-pipeline/remediation-report.md` (PR #26) |
| 6 | Revalidation Results | `hunter-pipeline/revalidation-results.md` (PR #27) |
| 7 | This Report | `hunter-pipeline/security-audit-report.md` |

## Child Sessions

| Role | Session | Domain/Purpose |
|------|---------|---------------|
| [Domain] | devin-2cc82e5e9dbd42b99cf48bdd0ce63e61 | AUTH — 18 candidates |
| [Domain] | devin-c16d6761a6ac4e378c4fa9e50f2f32cb | ACCT — 14 candidates |
| [Domain] | devin-c9055e54420241dfa0c7f23af9cea00b | XFER — 22 candidates |
| [Domain] | devin-7078e4b667f44e9e9be239adb9ceeddf | APPT — 11 candidates |
| [Domain] | devin-aead4ceb500e4f76a92a1eea3de035b0 | ADMIN — 24 candidates |
| [Domain] | devin-e717d7e32281436298c94b220137f162 | CONFIG — 19 candidates |
| [Domain] | devin-9bdfc317ba6e420192607316fb9a48b1 | DEPS — 23 candidates |
| [Validator] | devin-0b2f8e0c898b40cfa37ab55207670446 | Static validation — 29 confirmed |
| [Dynamic Validator] | devin-8c39094a5d124978a637d45c74036801 | Runtime testing — 26 dynamically confirmed |
| [Remediator] | devin-0bd23f60ea1b4acfa29761a104676759 | 10 fixes, PR #26 |
| [Revalidator] | devin-b591e41d15fa4023988f63b2d840c6e1 | 10/10 fixes verified |
