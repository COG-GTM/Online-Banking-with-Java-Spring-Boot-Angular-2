# CONFIG Domain Rollup — Configuration & Security Infrastructure

**Pipeline:** Hunter Pipeline
**Domain:** CONFIG
**Repo:** `COG-GTM/Online-Banking-with-Java-Spring-Boot-Angular-2`
**Stack:** Java 8, Spring Boot 1.5.4.RELEASE (EOL), Spring Security, Hibernate/JPA, MySQL
**Scan date:** 2026-06-04
**Vuln classes scanned:** MISCONFIG, CRYPTOFAIL, SDE (Sensitive Data Exposure)

---

## 1. Domain Brief

The CONFIG domain covers the application's security wiring, request filtering, and externalized
configuration for the `UserFront` Spring Boot module. Three files define the entire security and
configuration posture of the customer-facing banking backend:

| File | Role |
|------|------|
| `UserFront/src/main/java/com/userFront/config/SecurityConfig.java` | Spring Security: URL authorization, password encoding, CSRF/CORS toggles, form login, remember-me |
| `UserFront/src/main/java/com/userFront/config/RequestFilter.java` | Highest-precedence servlet filter manually injecting CORS headers for the Angular admin portal |
| `UserFront/src/main/resources/application.properties` | Datasource (MySQL) credentials, JPA/Hibernate behavior, SQL logging |

### Architectural observations

- **EOL framework.** The build inherits `spring-boot-starter-parent` **1.5.4.RELEASE**
  (`UserFront/pom.xml:14-19`), released June 2017 and long past end-of-life. Spring Boot 1.5.x,
  the bundled Spring Framework 4.3.x, Spring Security 4.2.x, and Tomcat 8.5.x lines carry numerous
  published CVEs that will never be patched on this branch. All transitive dependency versions are
  pinned by this EOL BOM (`mysql-connector-java`, `thymeleaf`, `jackson`, `tomcat-embed-*`, etc.).
- **Security is globally relaxed.** CSRF is disabled application-wide and Spring's CORS support is
  disabled in favor of a hand-rolled, permissive filter. The H2/DB console path is publicly exposed.
- **Secrets are hardcoded in source.** Both the database password and the BCrypt "salt" live in
  version-controlled files, so anyone with repo (or decompiled-jar) access obtains production-grade
  credentials and the encoder seed.
- **Cryptography is misused.** The password encoder is seeded with a static, attacker-known value,
  defeating the entire purpose of BCrypt's per-hash random salt.

These three files are individually small but collectively establish a weak baseline that amplifies
every other vulnerability class in the application (authn, authz, injection, session handling).

---

## 2. Vulnerability Candidates

> IDs use the format `CONFIG-{VULN}-{NNN}`. Candidates are intentionally over-generated; some overlap
> or are lower-confidence and are flagged accordingly.

### 2a. MISCONFIG — Security Misconfiguration

---

#### CONFIG-MISCONFIG-001 — CSRF protection disabled globally
- **Vuln class:** MISCONFIG
- **Severity:** High
- **Affected file(s):** `SecurityConfig.java:60` (`http.csrf().disable()`)
- **Description:** CSRF protection is turned off for the entire application. The app uses cookie/session
  based form login (`formLogin()` + `rememberMe()`), which is precisely the model CSRF tokens protect.
  State-changing banking operations (fund transfers, recipient creation, appointment booking,
  deposits/withdrawals) are all reachable via simple POST requests with no anti-CSRF token.
- **Exploit scenario:** A logged-in customer visits an attacker-controlled page. The page auto-submits
  a hidden form to `/transferBetweenAccounts` (or the recipient/transfer endpoints). Because the
  victim's session cookie is sent automatically and no CSRF token is required, the transfer executes
  with the victim's authority — silent account drain.

#### CONFIG-MISCONFIG-002 — Spring CORS disabled and replaced by an over-permissive manual filter
- **Vuln class:** MISCONFIG
- **Severity:** High
- **Affected file(s):** `SecurityConfig.java:60` (`.cors().disable()`); `RequestFilter.java:23-27`
- **Description:** Spring's managed CORS is disabled and a hand-written `RequestFilter` (highest
  precedence) injects `Access-Control-Allow-Origin: http://localhost:4200` together with
  `Access-Control-Allow-Credentials: true`. Combining a credentialed CORS policy with a filter that
  bypasses the security chain for preflight is fragile and error-prone, and the hardcoded localhost
  origin is a development artifact shipped to production.
- **Exploit scenario:** If the origin is ever widened (e.g. to `*` during debugging, a common "fix")
  while credentials remain allowed, any website can issue authenticated cross-origin requests and read
  responses, exfiltrating account data. Even as written, a developer running the Angular app locally
  against a production API is granted credentialed cross-origin access.

#### CONFIG-MISCONFIG-003 — Database/H2 console publicly accessible (`/console/**` permitAll)
- **Vuln class:** MISCONFIG
- **Severity:** Critical
- **Affected file(s):** `SecurityConfig.java:47` (`"/console/**"` in `PUBLIC_MATCHERS`), used at `:56`
- **Description:** The `/console/**` path is whitelisted with `permitAll()`, exposing the database/H2
  web console without authentication. Combined with CSRF disabled and frame options behavior, this
  yields an unauthenticated administrative data interface.
- **Exploit scenario:** An attacker browses to `/console`, connects to the backing database (credentials
  are also hardcoded — see CONFIG-CRYPTOFAIL-003 / SDE), and runs arbitrary SQL: dump all users and
  account balances, change balances, or create an admin user.

#### CONFIG-MISCONFIG-004 — CORS preflight short-circuits the security filter chain
- **Vuln class:** MISCONFIG
- **Severity:** Medium
- **Affected file(s):** `RequestFilter.java:16` (`@Order(Ordered.HIGHEST_PRECEDENCE)`), `:29-42`
- **Description:** Because the filter runs at highest precedence and returns `200 OK` for every
  `OPTIONS` request before the Spring Security chain executes, preflight handling is entirely outside
  security control. Any logic relying on OPTIONS being authenticated/authorized is bypassed, and the
  filter unconditionally sets permissive ACAO/ACAC headers on every response.
- **Exploit scenario:** Tooling or WAF rules that assume OPTIONS traverses the security chain are
  silently bypassed; the always-on credentialed CORS headers broaden the attack surface for
  CONFIG-MISCONFIG-002.

#### CONFIG-MISCONFIG-005 — `rememberMe()` configured with default key and persistent cookie
- **Vuln class:** MISCONFIG
- **Severity:** Medium
- **Affected file(s):** `SecurityConfig.java:65` (`.rememberMe()`)
- **Description:** Remember-me is enabled with no explicit `key(...)`, so Spring Security derives a
  default signing key. The hash-based remember-me token is therefore signed with a predictable/derivable
  key and tied to the user's (statically-salted) password hash. No `tokenValiditySeconds` hardening is
  set either.
- **Exploit scenario:** An attacker who can derive or guess the default key (or who obtains the password
  hash via the DB console / SQLi) can forge a valid remember-me cookie and authenticate as any user
  without the password.

#### CONFIG-MISCONFIG-006 — Hibernate `ddl-auto = update` in a production datasource
- **Vuln class:** MISCONFIG
- **Severity:** Medium
- **Affected file(s):** `application.properties:31` (`spring.jpa.hibernate.ddl-auto = update`)
- **Description:** Allowing Hibernate to mutate the live schema at startup based on entity definitions is
  unsafe for a banking system. Schema drift, accidental column changes, and unreviewed DDL can occur on
  every deploy, and it widens the blast radius of a compromised app instance.
- **Exploit scenario:** An attacker who can influence entity mappings (or a malicious/erroneous deploy)
  triggers destructive or additive DDL against the production database, enabling data corruption or
  privilege/schema manipulation.

#### CONFIG-MISCONFIG-007 — SQL query logging enabled (`show-sql = true`)
- **Vuln class:** MISCONFIG / SDE
- **Severity:** Low
- **Affected file(s):** `application.properties:26` (`spring.jpa.show-sql = true`)
- **Description:** Hibernate logs every SQL statement to stdout. In production this floods logs with
  query data (and bound parameter context depending on logger config), aiding reconnaissance and risking
  sensitive data landing in log aggregation.
- **Exploit scenario:** An attacker (or insider) with log access reconstructs schema and queries against
  account tables, accelerating injection and data-targeting attacks.

#### CONFIG-MISCONFIG-008 — Deprecated/EOL security framework configuration model
- **Vuln class:** MISCONFIG
- **Severity:** Medium
- **Affected file(s):** `SecurityConfig.java:14,23` (`WebSecurityConfigurerAdapter`); `pom.xml:14-19`
- **Description:** The config extends the removed-in-modern-Spring `WebSecurityConfigurerAdapter` and
  relies on the Spring Boot 1.5.x security defaults. Beyond being unmaintainable, the EOL Spring
  Security 4.2.x defaults lack many later hardening defaults (e.g. modern security headers, secure
  session/cookie defaults).
- **Exploit scenario:** Missing/old default security headers (HSTS, frame options behavior, content-type
  options) leave the app exposed to clickjacking and downgrade attacks the framework would otherwise
  mitigate by default in supported versions.

#### CONFIG-MISCONFIG-009 — No HTTPS/transport enforcement (`requiresChannel`) configured
- **Vuln class:** MISCONFIG
- **Severity:** Medium
- **Affected file(s):** `SecurityConfig.java:52-66` (entire `configure(HttpSecurity)`)
- **Description:** There is no `requiresChannel().anyRequest().requiresSecure()` or equivalent TLS
  enforcement, and no HSTS configuration. Form-login credentials and session/remember-me cookies can be
  transmitted over plaintext HTTP.
- **Exploit scenario:** A network attacker (e.g. shared Wi-Fi) intercepts login credentials or session
  cookies in transit and hijacks the banking session.

---

### 2b. CRYPTOFAIL — Cryptographic Failures

---

#### CONFIG-CRYPTOFAIL-001 — Static, hardcoded BCrypt salt seed defeats per-hash randomness
- **Vuln class:** CRYPTOFAIL
- **Severity:** Critical
- **Affected file(s):** `SecurityConfig.java:31` (`SALT = "salt"`), `:34-36`
  (`new BCryptPasswordEncoder(12, new SecureRandom(SALT.getBytes()))`)
- **Description:** `BCryptPasswordEncoder` is given a `SecureRandom` seeded with the constant string
  `"salt"`. Seeding `SecureRandom` with a fixed value makes its output deterministic, so the salt BCrypt
  generates for every password is effectively predictable/identical across the deployment. This defeats
  BCrypt's core defense — unique random per-password salts — and makes hashes vulnerable to precomputation
  and cross-user comparison.
- **Exploit scenario:** With a known, fixed salt an attacker precomputes a rainbow/dictionary table once
  and cracks every stolen hash offline. Identical passwords also produce identical hashes, instantly
  revealing which users share a password.

#### CONFIG-CRYPTOFAIL-002 — Cryptographic salt committed to source control
- **Vuln class:** CRYPTOFAIL / SDE
- **Severity:** High
- **Affected file(s):** `SecurityConfig.java:31`
- **Description:** Even setting aside the misuse, the salt value is a hardcoded constant in a
  version-controlled file (and shipped inside the compiled jar). Secret cryptographic material must not
  be embedded in source; it cannot be rotated without a redeploy and is exposed to anyone with repo or
  artifact access.
- **Exploit scenario:** An attacker who reads the repo or decompiles the jar obtains the salt seed,
  removing any residual uncertainty when cracking the leaked password hashes.

#### CONFIG-CRYPTOFAIL-003 — Plaintext database password in configuration
- **Vuln class:** CRYPTOFAIL / SDE
- **Severity:** Critical
- **Affected file(s):** `application.properties:11-12` (`username = root`, `password = avengers1993`)
- **Description:** The MySQL credentials are stored in cleartext in a checked-in properties file, using
  the all-privilege `root` account. There is no externalization (env vars, vault, encrypted config).
- **Exploit scenario:** Anyone with repository read access, build-artifact access, or a path traversal /
  file disclosure bug obtains full `root` database access and can read or modify all customer and
  financial data.

#### CONFIG-CRYPTOFAIL-004 — Database connection uses unencrypted transport (no TLS / `useSSL`)
- **Vuln class:** CRYPTOFAIL
- **Severity:** Medium
- **Affected file(s):** `application.properties:8`
  (`jdbc:mysql://localhost:3306/onlinebanking` — no `useSSL`/`requireSSL`/`sslMode`)
- **Description:** The JDBC URL specifies no TLS parameters. Depending on the connector default this may
  result in an unencrypted DB connection carrying credentials and sensitive financial records in
  cleartext over the network.
- **Exploit scenario:** An attacker positioned between the app and database server sniffs credentials and
  account data, or performs a MITM to tamper with query results.

#### CONFIG-CRYPTOFAIL-005 — Overly privileged `root` DB account (least-privilege failure)
- **Vuln class:** CRYPTOFAIL / MISCONFIG
- **Severity:** High
- **Affected file(s):** `application.properties:11`
- **Description:** The application authenticates to MySQL as `root`, granting it full administrative
  control over the database server rather than a scoped application user limited to the `onlinebanking`
  schema.
- **Exploit scenario:** Any application-level compromise (SQLi, exposed `/console`, RCE) is immediately
  amplified to full database-server takeover, including other databases on the same instance.

#### CONFIG-CRYPTOFAIL-006 — No password/credential rotation or externalization mechanism
- **Vuln class:** CRYPTOFAIL
- **Severity:** Low
- **Affected file(s):** `application.properties:11-12`; `SecurityConfig.java:31`
- **Description:** Because both DB password and crypto salt are compile-time constants, there is no
  supported rotation path. A leak requires a code change + redeploy to remediate, violating standard key
  management practice.
- **Exploit scenario:** After any leak, the long remediation window leaves stolen credentials/salt valid
  and exploitable for an extended period.

---

### 2c. SDE — Sensitive Data Exposure

---

#### CONFIG-SDE-001 — Hardcoded DB credentials exposed in repo/artifact
- **Vuln class:** SDE
- **Severity:** Critical
- **Affected file(s):** `application.properties:11-12`
- **Description:** (See CONFIG-CRYPTOFAIL-003 for the crypto angle.) From an exposure standpoint, live
  production credentials (`root` / `avengers1993`) are disclosed to every party with source, CI, or jar
  access, and to anyone able to retrieve the properties file at runtime.
- **Exploit scenario:** Credential harvested from a public/forked repo or leaked build artifact is reused
  directly against the database, or sprayed against other systems if reused.

#### CONFIG-SDE-002 — Stack traces printed to stdout via `printStackTrace()`
- **Vuln class:** SDE
- **Severity:** Medium
- **Affected file(s):** `RequestFilter.java:32-34` (`catch(Exception e){ e.printStackTrace(); }`)
- **Description:** Exceptions thrown while servicing requests are swallowed and dumped via
  `printStackTrace()`. This leaks internal class names, package structure, and stack details into logs,
  and the swallow-and-continue pattern can mask security-relevant failures.
- **Exploit scenario:** An attacker triggers errors to enumerate framework versions and internal code
  paths via log access (or any surface that reflects these traces), supporting targeted exploitation.

#### CONFIG-SDE-003 — Verbose SQL/diagnostic logging leaks data structure
- **Vuln class:** SDE
- **Severity:** Low
- **Affected file(s):** `application.properties:26` (`show-sql = true`)
- **Description:** (Overlaps CONFIG-MISCONFIG-007.) Continuous SQL logging exposes schema and query
  patterns over account/transaction tables to anyone with log visibility.
- **Exploit scenario:** Insider or log-pipeline attacker maps the data model and crafts precise queries
  against sensitive financial tables.

#### CONFIG-SDE-004 — `Pre-flight` debug output via `System.out.println`
- **Vuln class:** SDE
- **Severity:** Informational
- **Affected file(s):** `RequestFilter.java:36` (`System.out.println("Pre-flight");`)
- **Description:** Leftover debug printing in a request-path filter. Low direct impact, but indicates
  debug code shipped to production and contributes to log noise that can hide real signals.
- **Exploit scenario:** Minimal direct exploit; serves as a reconnaissance indicator of debug builds and
  degrades log integrity/monitoring.

#### CONFIG-SDE-005 — Predictable crypto salt enables hash de-anonymization
- **Vuln class:** SDE / CRYPTOFAIL
- **Severity:** High
- **Affected file(s):** `SecurityConfig.java:31-36`
- **Description:** (Exposure consequence of CONFIG-CRYPTOFAIL-001.) Because salts are effectively fixed,
  equal passwords yield equal hashes, exposing the relationship "these users share a password" directly
  from a hash dump.
- **Exploit scenario:** From a leaked user table an attacker clusters identical hashes, cracks one, and
  instantly compromises every account in that cluster.

---

## 3. Domain Rollup

### Candidate inventory

| ID | Title | Class | Severity |
|----|-------|-------|----------|
| CONFIG-MISCONFIG-001 | CSRF disabled globally | MISCONFIG | High |
| CONFIG-MISCONFIG-002 | Spring CORS disabled; permissive manual filter | MISCONFIG | High |
| CONFIG-MISCONFIG-003 | `/console/**` DB console publicly accessible | MISCONFIG | Critical |
| CONFIG-MISCONFIG-004 | CORS preflight short-circuits security chain | MISCONFIG | Medium |
| CONFIG-MISCONFIG-005 | `rememberMe()` default key / persistent cookie | MISCONFIG | Medium |
| CONFIG-MISCONFIG-006 | Hibernate `ddl-auto = update` in prod | MISCONFIG | Medium |
| CONFIG-MISCONFIG-007 | SQL logging enabled (`show-sql`) | MISCONFIG/SDE | Low |
| CONFIG-MISCONFIG-008 | EOL/deprecated security framework + defaults | MISCONFIG | Medium |
| CONFIG-MISCONFIG-009 | No HTTPS/HSTS transport enforcement | MISCONFIG | Medium |
| CONFIG-CRYPTOFAIL-001 | Static seed defeats BCrypt random salt | CRYPTOFAIL | Critical |
| CONFIG-CRYPTOFAIL-002 | Crypto salt committed to source | CRYPTOFAIL/SDE | High |
| CONFIG-CRYPTOFAIL-003 | Plaintext DB password in config | CRYPTOFAIL/SDE | Critical |
| CONFIG-CRYPTOFAIL-004 | DB connection without TLS (`useSSL`) | CRYPTOFAIL | Medium |
| CONFIG-CRYPTOFAIL-005 | Overly privileged `root` DB account | CRYPTOFAIL/MISCONFIG | High |
| CONFIG-CRYPTOFAIL-006 | No credential/key rotation mechanism | CRYPTOFAIL | Low |
| CONFIG-SDE-001 | Hardcoded DB credentials exposed | SDE | Critical |
| CONFIG-SDE-002 | `printStackTrace()` leaks internals | SDE | Medium |
| CONFIG-SDE-003 | Verbose SQL logging leaks structure | SDE | Low |
| CONFIG-SDE-004 | `System.out.println` debug output | SDE | Informational |
| CONFIG-SDE-005 | Predictable salt enables hash de-anonymization | SDE/CRYPTOFAIL | High |

### Severity distribution

| Severity | Count |
|----------|-------|
| Critical | 4 |
| High | 5 |
| Medium | 6 |
| Low | 3 |
| Informational | 1 |
| **Total** | **19** |

> Note: several candidates are cross-listed (e.g. hardcoded DB password appears as both
> CONFIG-CRYPTOFAIL-003 and CONFIG-SDE-001) because they belong to multiple vuln classes; they
> represent ~16 distinct root issues.

### Top priorities (fix first)

1. **CONFIG-CRYPTOFAIL-001** — Remove the static `SecureRandom(SALT)` seed; use the default
   `BCryptPasswordEncoder(strength)` constructor so BCrypt generates a unique random salt per hash.
   (All existing hashes must be re-hashed on next login.)
2. **CONFIG-MISCONFIG-003** — Remove `/console/**` from public matchers (or disable the console entirely
   in production).
3. **CONFIG-CRYPTOFAIL-003 / CONFIG-SDE-001** — Externalize DB credentials (env vars / vault), rotate the
   leaked `root` password, and switch to a least-privilege application DB user (also CONFIG-CRYPTOFAIL-005).
4. **CONFIG-MISCONFIG-001** — Re-enable CSRF protection for state-changing endpoints.
5. **CONFIG-MISCONFIG-002 / -004** — Replace the manual CORS filter with Spring-managed CORS scoped to a
   configurable, non-wildcard origin and avoid credentialed wildcard combinations.

### Cross-domain notes

- The EOL Spring Boot 1.5.4 BOM (`pom.xml:14-19`) is an upstream dependency/CVE concern that overlaps
  with any DEPS/SCA domain — recommend a dedicated SCA scan (e.g. Snyk `snyk_sca_scan`) for transitive
  CVEs.
- `/console/**` exposure compounds with the hardcoded DB credentials and any AUTHZ/INJECTION findings —
  flag for correlation in the final pipeline merge.

---

*Generated by the CONFIG domain scanner. Findings are candidates for triage; severities are scanner
estimates pending confirmation.*
