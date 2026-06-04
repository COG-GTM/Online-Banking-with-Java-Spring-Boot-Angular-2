# Validated Findings — Online Banking Hunter Pipeline (Static Validation)

**Repo:** `COG-GTM/Online-Banking-with-Java-Spring-Boot-Angular-2`
**Stack:** Java 8, Spring Boot 1.5.4.RELEASE (EOL), Spring Security 4.2.x (form-based session auth), Hibernate 5.0.x, Thymeleaf 2.1.x, MySQL, Angular 4 (AdminPortal)
**Validation date:** 2026-06-04
**Validator role:** Static validator — applied a 3-check scorecard (Reachability, Constrained Persona, Exploit Path) to each candidate and classified it. No code was changed; this is a triage deliverable.

---

## How to read this document

Each candidate is validated against the **actual source** using a 3-check scorecard:

1. **Reachability** — can external user input reach the vulnerable code via controller → service → DAO/entity? (`reachable` / `conditionally_reachable` / `unreachable`). For configuration/secret-exposure candidates that have no HTTP sink, reachability is reported as `n/a (disclosure)` and the finding is judged on whether the exposure is real.
2. **Constrained Persona** — who can trigger it? (`any_user` = any authenticated regular customer, or unauthenticated where noted; `admin_only` = requires ROLE_ADMIN; `system_only` = requires repo/artifact/host/DB-network access, not an HTTP actor).
3. **Exploit Path** — step-by-step from entry point to impact.

**Classification taxonomy:**
- `confirmed_exploitable` — all 3 checks pass; a clear exploit path exists in this codebase.
- `likely_exploitable` — reachability confirmed; exploit path plausible but needs runtime verification.
- `potential_issue` — code smell / defense-in-depth / EOL-or-vulnerable dependency present but the specific exploit vector is not reachable in this app's configuration.
- `false_positive` — unreachable, mitigated by other controls, or the original analysis is incorrect in this context.

---

## Summary table (counts per classification)

Counts are over the **85 individually-classified candidate entries** written up below (CONFIG 20, DEPS 23, ADMIN 24, AUTH 6, ACCT 4, XFER 3, APPT 5). Cross-listed candidate IDs are counted separately because each is a distinct ID in the catalog (e.g. the static BCrypt salt appears as CONFIG-CRYPTOFAIL-001, ADMIN-SDE-003, and AUTH-CRYPTOFAIL-001; CSRF-disabled appears in CONFIG, ADMIN, XFER and AUTH). This excludes the 5 attack chains (§9) and the 4 categorical "remaining items" mapping buckets for the non-enumerated domains (§8). De-duplicated, these resolve to roughly **70 distinct root issues**.

| Classification | Count |
|---|---|
| confirmed_exploitable | 29 |
| likely_exploitable | 3 |
| potential_issue | 49 |
| false_positive | 4 |
| **Total classified entries** | **85** |

### Counts restricted to Critical + High candidates (the validation priority)

| Classification | Count (Crit+High) |
|---|---|
| confirmed_exploitable | 23 |
| likely_exploitable | 3 |
| potential_issue | 20 |
| false_positive | 3 |

### Headline confirmed-exploitable findings (fix first)

| Finding | Severity | Persona | Why it matters |
|---|---|---|---|
| ADMIN-SDE-001 / APPT-IDOR-004 | Critical | admin_only | `/api/user/all` **and** `/api/appointment/all` serialize raw `User` entities → BCrypt password hashes leak in JSON |
| CONFIG-CRYPTOFAIL-001 | Critical | any_user | `SecureRandom` seeded with constant `"salt"` → every BCrypt hash is deterministic; identical passwords ⇒ identical hashes |
| ACCT-DEPOSIT-001 | High* | any_user | `deposit()` credits arbitrary unfunded amounts with no validation → a customer mints money into their own balance |
| XFER-INJ-002 | Critical | any_user | Negative transfer amount → `subtract(negative)` = **add** → self-credit |
| AUTH-AUTHZ-001 | High | any_user | `/profile` POST loads the target user by request-supplied `username` (IDOR) → edit any customer's PII |
| XFER-IDOR-001 | High | any_user | `findRecipientByName` / `deleteRecipientByName` are global → read/delete any user's recipients (incl. account numbers) |
| ADMIN-IDOR-001/002/003 | High | admin_only | `/api/user/...` endpoints take an attacker-supplied `username` with no scoping → any customer's transactions / enable-disable |
| CONFIG-CRYPTOFAIL-003 | Critical | system_only | Plaintext `root` / `avengers1993` MySQL credentials committed to the repo |
| CONFIG-MISCONFIG-001 (CSRF off) | High | any_user | CSRF disabled app-wide + many state-changing ops reachable via GET → drive-by transfers/profile edits |

\* Business-logic severity; the scanner did not pre-rate the derived ACCT logic candidates.

---

## §1. CONFIG domain (19 candidates) — security wiring, crypto, secrets

> Files: `SecurityConfig.java`, `RequestFilter.java`, `application.properties`. All claims verified against source.

### CONFIG-MISCONFIG-001 — CSRF protection disabled globally
- **Original severity:** High
- **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** any_user — **Confidence:** high
- **Exploit path:** `SecurityConfig.java:60` `http.csrf().disable()`. App uses cookie/session form login + `rememberMe()`. A logged-in customer visits an attacker page that auto-submits `POST /transfer/toSomeoneElse` (or `/profile`, `/transfer/recipient/save`, `/account/deposit`). The session cookie rides along; no token is required; the request executes with the victim's authority. Many state-changing endpoints (recipient edit/delete, appointment confirm, user enable/disable) are additionally reachable via **GET**, so even an `<img src>` triggers them.
- **Notes:** Root enabler that upgrades several other findings to "drive-by." See cross-domain chain CHAIN-1.

### CONFIG-MISCONFIG-002 — Spring CORS disabled, replaced by permissive manual filter
- **Original severity:** High
- **Classification:** `potential_issue`
- **Reachability:** conditionally_reachable — **Persona:** n/a — **Confidence:** medium
- **Exploit path:** `SecurityConfig.java:60` `.cors().disable()`; `RequestFilter.java:23-27` sets `Access-Control-Allow-Origin: http://localhost:4200` + `Access-Control-Allow-Credentials: true`. As written the ACAO is a **fixed** value (not reflected from the request `Origin`), so an arbitrary attacker origin is **not** granted credentialed read access — the browser blocks it. Real risk is fragility: if anyone widens the origin to `*` or reflects the request origin (a common "fix") while keeping credentials, it becomes a credentialed-CORS account-data exfiltration bug.
- **Notes:** Downgraded from directly-exploitable to defense-in-depth/fragile-config.

### CONFIG-MISCONFIG-003 — `/console/**` permitAll (DB/H2 console exposed)
- **Original severity:** Critical
- **Classification:** `false_positive`
- **Reachability:** unreachable — **Persona:** n/a — **Confidence:** high
- **Exploit path:** `SecurityConfig.java:47` whitelists `/console/**`. However there is **no H2 dependency** in `UserFront/pom.xml` and **no `ServletRegistrationBean`/console servlet** mapped to `/console`. The path resolves to nothing (404). The `permitAll` is dead config; no unauthenticated DB console exists.
- **Notes:** This same Critical claim is echoed as `ACCT-INJ-001` and `AUTH` console items — all false positives for the same reason. Latent: would become real if an H2 console were ever added.

### CONFIG-MISCONFIG-004 — CORS preflight short-circuits the security chain
- **Original severity:** Medium
- **Classification:** `potential_issue`
- **Reachability:** conditionally_reachable — **Persona:** n/a — **Confidence:** medium
- **Exploit path:** `RequestFilter.java:16,29-42` — filter at `HIGHEST_PRECEDENCE` returns `200` for every `OPTIONS` before Spring Security runs. OPTIONS is non-state-changing here, so no direct impact; concern is bypass of any tooling/WAF that assumes OPTIONS traverses the chain, plus always-on credentialed CORS headers.

### CONFIG-MISCONFIG-005 — `rememberMe()` with default key / persistent cookie
- **Original severity:** Medium
- **Classification:** `potential_issue`
- **Reachability:** conditionally_reachable — **Persona:** any_user — **Confidence:** medium
- **Exploit path:** `SecurityConfig.java:65` `.rememberMe()` with no explicit `key(...)` or `tokenValiditySeconds`. **Correction to candidate:** when no key is supplied, Spring Security generates a **random** key at startup (not a static, predictable default), so the cookie cannot be forged from source knowledge alone. Real risk: the token is a hash of `username:expiry:password-hash:key`; combined with the static-salt hash exposure (CONFIG-CRYPTOFAIL-001 + ADMIN-SDE-001) and the runtime key, forgery becomes feasible, and the default 14-day validity is long.
- **Notes:** Becomes a real forgery path only in chain with a hash leak; standalone it is a hardening gap.

### CONFIG-MISCONFIG-006 — Hibernate `ddl-auto = update` in production
- **Original severity:** Medium
- **Classification:** `potential_issue`
- **Reachability:** n/a (disclosure/operational) — **Persona:** system_only — **Confidence:** high
- **Exploit path:** `application.properties:31`. No external HTTP path; operational risk (schema drift, destructive auto-DDL). Confirmed present.

### CONFIG-MISCONFIG-007 — SQL query logging enabled (`show-sql = true`)
- **Original severity:** Low
- **Classification:** `potential_issue`
- **Reachability:** n/a (disclosure) — **Persona:** system_only — **Confidence:** high
- **Exploit path:** `application.properties:26`. Logs SQL to stdout; recon aid for anyone with log access. Confirmed present. (Duplicated as CONFIG-SDE-003.)

### CONFIG-MISCONFIG-008 — EOL/deprecated security framework config model
- **Original severity:** Medium
- **Classification:** `potential_issue`
- **Reachability:** n/a — **Persona:** n/a — **Confidence:** medium
- **Exploit path:** `SecurityConfig.java` extends `WebSecurityConfigurerAdapter` on Spring Security 4.2.x (EOL). **Correction:** Spring Security 4.2.x **does** set `X-Frame-Options: DENY` and `X-Content-Type-Options: nosniff` by default, so the "missing default headers → clickjacking" claim is overstated; HSTS is only emitted over HTTPS (see -009). The genuine issue is EOL/unmaintained framework.

### CONFIG-MISCONFIG-009 — No HTTPS/HSTS transport enforcement
- **Original severity:** Medium
- **Classification:** `potential_issue`
- **Reachability:** conditionally_reachable — **Persona:** any_user (network position) — **Confidence:** medium
- **Exploit path:** No `requiresChannel().requiresSecure()` / HSTS in `configure(HttpSecurity)`. If deployed without an HTTPS-terminating proxy, credentials/session cookies traverse plaintext and can be sniffed. Deployment-dependent.

### CONFIG-CRYPTOFAIL-001 — Static `SecureRandom` seed defeats BCrypt per-hash salt
- **Original severity:** Critical
- **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** any_user — **Confidence:** high
- **Exploit path:** `SecurityConfig.java:31-36` — `new BCryptPasswordEncoder(12, new SecureRandom(SALT.getBytes()))`. Seeding `SecureRandom` with constant `"salt"` makes salt generation deterministic. Any registration (`POST /signup`, public) hashes the user password through this encoder, so **identical passwords produce identical hashes** across the deployment, and the salt is precomputable. Combined with a hash dump (ADMIN-SDE-001), an attacker clusters equal-password accounts and cracks them offline against one precomputed table.
- **Notes:** The crypto defect is definite from code; "exploit" is realized when hashes leak (which they do — ADMIN-SDE-001). Same root issue as ADMIN-SDE-003, AUTH-CRYPTOFAIL-001, CONFIG-SDE-005.

### CONFIG-CRYPTOFAIL-002 — Crypto salt committed to source control
- **Original severity:** High
- **Classification:** `potential_issue`
- **Reachability:** n/a (disclosure) — **Persona:** system_only — **Confidence:** high
- **Exploit path:** `SecurityConfig.java:31` constant `SALT="salt"` in VCS/jar. No HTTP path; amplifies offline cracking once hashes leak. Real exposure but no standalone exploit path → defense-in-depth amplifier.

### CONFIG-CRYPTOFAIL-003 — Plaintext DB password in configuration
- **Original severity:** Critical
- **Classification:** `confirmed_exploitable`
- **Reachability:** n/a (source/artifact disclosure) — **Persona:** system_only — **Confidence:** high
- **Exploit path:** `application.properties:11-12` — `username=root`, `password=avengers1993` committed in cleartext using the all-privilege MySQL `root` account. Anyone with repo (this is a public-style fork), CI, or jar access — or a file-disclosure bug — obtains full DB control: dump/modify all customer + financial data, mint admin users. The secret is genuinely present and usable, so this is a confirmed sensitive-data exposure (not merely "potential"), despite having no HTTP sink.
- **Notes:** Same root issue as CONFIG-SDE-001. Remediate by rotating the credential and externalizing to env/vault.

### CONFIG-CRYPTOFAIL-004 — DB connection without TLS (`useSSL`)
- **Original severity:** Medium
- **Classification:** `potential_issue`
- **Reachability:** conditionally_reachable — **Persona:** system_only (network) — **Confidence:** medium
- **Exploit path:** `application.properties:8` JDBC URL has no `useSSL`/`sslMode`. MITM between app and DB could sniff/tamper. Network-position dependent (localhost in this config).

### CONFIG-CRYPTOFAIL-005 — Overly privileged `root` DB account
- **Original severity:** High
- **Classification:** `potential_issue`
- **Reachability:** n/a — **Persona:** system_only — **Confidence:** high
- **Exploit path:** `application.properties:11`. App authenticates as MySQL `root`. Blast-radius amplifier: any app-level DB compromise becomes full DB-server takeover. Confirmed; least-privilege failure.

### CONFIG-CRYPTOFAIL-006 — No credential/key rotation mechanism
- **Original severity:** Low
- **Classification:** `potential_issue`
- **Reachability:** n/a — **Persona:** n/a — **Confidence:** high
- **Exploit path:** DB password and salt are compile-time constants; remediating a leak requires code change + redeploy. Process/key-management gap.

### CONFIG-SDE-001 — Hardcoded DB credentials exposed in repo/artifact
- **Original severity:** Critical
- **Classification:** `confirmed_exploitable`
- **Reachability:** n/a (disclosure) — **Persona:** system_only — **Confidence:** high
- **Exploit path:** Same root issue as CONFIG-CRYPTOFAIL-003 (`root`/`avengers1993`), from the exposure angle. Confirmed.

### CONFIG-SDE-002 — `printStackTrace()` leaks internals / swallows errors
- **Original severity:** Medium
- **Classification:** `potential_issue`
- **Reachability:** unreachable (traces go to stdout, not HTTP responses) — **Persona:** system_only — **Confidence:** high
- **Exploit path:** `RequestFilter.java:32-34`. Stack traces to stdout; swallow-and-continue can mask failures. Recon aid via log access; no client-facing leak path. Confirmed present.

### CONFIG-SDE-003 — Verbose SQL logging leaks data structure
- **Original severity:** Low
- **Classification:** `potential_issue` (duplicate of CONFIG-MISCONFIG-007)
- **Reachability:** n/a — **Persona:** system_only — **Confidence:** high

### CONFIG-SDE-004 — `System.out.println("Pre-flight")` debug output
- **Original severity:** Informational
- **Classification:** `potential_issue`
- **Reachability:** n/a — **Persona:** system_only — **Confidence:** high
- **Exploit path:** `RequestFilter.java:36`. Debug noise; indicates debug code shipped. Negligible direct impact.

### CONFIG-SDE-005 — Predictable salt enables hash de-anonymization
- **Original severity:** High
- **Classification:** `confirmed_exploitable` (consequence of CONFIG-CRYPTOFAIL-001)
- **Reachability:** reachable (in chain with a hash dump) — **Persona:** admin_only (to obtain the dump) — **Confidence:** high
- **Exploit path:** Because salts are effectively fixed, equal passwords ⇒ equal hashes. Given the hash dump from `/api/user/all` (ADMIN-SDE-001), an attacker clusters identical hashes, cracks one, and compromises the whole cluster.

---

## §2. DEPS domain (23 candidates) — vulnerable / EOL dependencies

> All resolved versions confirmed via `mvn dependency:tree` against the Spring Boot 1.5.4 parent BOM. The library-in-use check passes for every backend candidate. Each is then judged on **whether the specific CVE vector is reachable in this app**. Frontend transitive versions are unpinned (no lockfile — DEPS-023).

> **General disposition:** the named vulnerable versions are genuinely present, but for nearly all of the "Critical RCE" libraries the exploit *vector* (default-typing deserialization, STOMP messaging, AJP, untrusted YAML/HQL/EL) is **not wired up** in this application. They are therefore `potential_issue` (real EOL/vulnerable dependency, no reachable sink) rather than `confirmed_exploitable`. Upgrading the Spring Boot BOM (DEPS-006) remediates the bulk at once.

### DEPS-DEPS-001 — jackson-databind 2.8.8 polymorphic deserialization RCE
- **Original severity:** Critical — **Classification:** `potential_issue`
- **Reachability:** unreachable — **Persona:** n/a — **Confidence:** high
- **Validation:** Version 2.8.8 confirmed on classpath (transitive via `starter-web`). The RCE CVEs require polymorphic/default typing (`enableDefaultTyping()` / `@JsonTypeInfo`) on a deserialized type. None exists; controllers consume form-urlencoded `@ModelAttribute`/`@RequestParam`, and the REST API is read-only output. No attacker-controlled polymorphic JSON sink → vector not reachable. Upgrade still recommended.

### DEPS-DEPS-002 — Spring Framework 4.3.9 SpEL RCE (+ Spring4Shell adjacency)
- **Original severity:** Critical — **Classification:** `potential_issue`
- **Reachability:** unreachable — **Persona:** n/a — **Confidence:** high
- **Validation:** 4.3.9 confirmed. CVE-2018-1270 needs STOMP/WebSocket messaging (not used). Spring4Shell (CVE-2022-22965) needs a WAR on Tomcat with JDK 9+; this is an executable JAR on **Java 8** with embedded Tomcat → classic vector mitigated. EOL framework stands as a hygiene/`potential_issue`.

### DEPS-DEPS-003 — Spring Data Commons 1.13.4 SpEL injection RCE (CVE-2018-1273)
- **Original severity:** Critical — **Classification:** `potential_issue`
- **Reachability:** unreachable — **Persona:** n/a — **Confidence:** medium
- **Validation:** 1.13.4 confirmed (vulnerable range 1.13.0–1.13.10). CVE-2018-1273 triggers when request parameters bind to **property paths** evaluated as SpEL (Spring Data web binding / Spring Data REST projections). This app binds explicit `@RequestParam`/`@ModelAttribute` to entities and calls derived finders; there is no property-path request binding or Spring Data REST exposure → vector not reachable. **Corrects** the rollup's "directly applicable" claim. Upgrade recommended.

### DEPS-DEPS-004 — Embedded Tomcat 8.5.15 Ghostcat + RCE cluster
- **Original severity:** Critical — **Classification:** `potential_issue`
- **Reachability:** unreachable — **Persona:** n/a — **Confidence:** high
- **Validation:** 8.5.15 confirmed. Ghostcat (CVE-2020-1938) needs the **AJP connector** enabled — embedded Boot does not enable AJP by default. Session-persistence deser (CVE-2020-9484), JSP-upload (CVE-2017-12617), CGI (CVE-2019-0232) require features/config not used here. EOL stands.

### DEPS-DEPS-005 — SnakeYAML 1.17 deserialization RCE + DoS
- **Original severity:** Critical — **Classification:** `potential_issue`
- **Reachability:** unreachable — **Persona:** n/a — **Confidence:** high
- **Validation:** 1.17 confirmed. RCE (CVE-2022-1471) needs the app to parse **untrusted** YAML with the default `Constructor`. The app parses only its own `application.properties` (not YAML, not attacker-controlled). No untrusted-YAML sink.

### DEPS-DEPS-006 — Spring Boot 1.5.4 EOL BOM (umbrella)
- **Original severity:** High — **Classification:** `potential_issue`
- **Reachability:** n/a — **Persona:** n/a — **Confidence:** high
- **Validation:** Confirmed EOL (Aug 2019). This BOM is the **root cause** pinning every vulnerable transitive version above; it is the single highest-leverage remediation (migrate to a supported Boot line). Not itself an exploit.

### DEPS-DEPS-007 — Hibernate ORM 5.0.12 SQL injection
- **Original severity:** High — **Classification:** `potential_issue`
- **Reachability:** unreachable — **Persona:** n/a — **Confidence:** high
- **Validation:** 5.0.12 confirmed. CVE-2019-14900 needs attacker-controlled **HQL literals**; CVE-2020-25638 needs `hibernate.use_sql_comments=true`. App uses only Spring Data **derived** queries (parameterized) — no `@Query`/HQL, and `use_sql_comments` is unset. SQLi vector not reachable; dependency is genuinely EOL.

### DEPS-DEPS-008 — Hibernate Validator 5.3.5 EL injection (CVE-2017-7536)
- **Original severity:** High — **Classification:** `potential_issue`
- **Reachability:** unreachable — **Persona:** n/a — **Confidence:** medium
- **Validation:** 5.3.5 confirmed. Exploit needs user-influenced data interpolated into a Bean Validation constraint **message**. Source grep confirms **no Bean Validation** is used at all (no `@Valid`, `javax.validation`, constraint annotations). Vector not reachable.

### DEPS-DEPS-009 — Spring Security 4.2.3 privilege escalation / bypass
- **Original severity:** High — **Classification:** `potential_issue`
- **Reachability:** n/a — **Persona:** n/a — **Confidence:** medium
- **Validation:** 4.2.3 confirmed (EOL). CVE-2018-1258 requires Spring Framework 5.0.5 pairing (not present); CVE-2018-1199 is static-resource bypass; CVE-2022-22978 is a 5.x `RegexRequestMatcher` issue (adjacency only). No reachable bypass identified in the 4.2.x usage here. EOL stands.

### DEPS-DEPS-010 — MySQL Connector/J 5.1.42
- **Original severity:** High — **Classification:** `potential_issue`
- **Reachability:** conditionally_reachable — **Persona:** system_only — **Confidence:** medium
- **Validation:** 5.1.42 confirmed (EOL). Deserialization CVEs require a malicious/compromised DB server or MITM. **Corrects** rollup: CVE-2023-21971 / CVE-2021-2471 apply to 8.0.x, not 5.1.42 — but 5.1.x has its own EOL CVE set. Conditional on hostile DB/network.

### DEPS-DEPS-011 — Logback 1.1.11 JNDI lookup RCE (CVE-2021-42550)
- **Original severity:** High — **Classification:** `potential_issue`
- **Reachability:** unreachable — **Persona:** system_only — **Confidence:** high
- **Validation:** 1.1.11 confirmed. Exploit requires the attacker to control/inject the **logback configuration** (not externally reachable in this app). No untrusted config sink.

### DEPS-DEPS-012 — Groovy 2.4.11 insecure temp-dir info disclosure (CVE-2020-17521)
- **Original severity:** Medium — **Classification:** `potential_issue`
- **Reachability:** unreachable — **Persona:** system_only — **Confidence:** medium
- **Validation:** 2.4.11 confirmed (transitive via thymeleaf-layout-dialect). No runtime Groovy compilation / temp-file API with sensitive data in this app. Vector not reachable.

### DEPS-DEPS-013 — Thymeleaf 2.1.5 EOL (SSTI surface)
- **Original severity:** Medium — **Classification:** `potential_issue`
- **Reachability:** unreachable — **Persona:** n/a — **Confidence:** medium
- **Validation:** 2.1.5 confirmed (EOL). SSTI needs user-controlled expression/fragment values; templates are static and `th:utext` is **absent** (grep-confirmed) so output auto-escaping is intact. No reachable SSTI/XSS sink. EOL stands.

### DEPS-DEPS-014 — OGNL 3.0.8 EOL
- **Original severity:** Medium — **Classification:** `potential_issue`
- **Reachability:** unreachable — **Persona:** n/a — **Confidence:** medium
- **Validation:** Transitive via Thymeleaf; no direct user-controlled OGNL evaluation. EOL hygiene.

### DEPS-DEPS-015 — Test-scope libs (json-smart / json-path / JUnit)
- **Original severity:** Medium (test scope) — **Classification:** `false_positive` (for production exposure)
- **Reachability:** unreachable — **Persona:** n/a — **Confidence:** high
- **Validation:** Pulled only via `spring-boot-starter-test`; not in the production runtime classpath. No production exposure. CI-hygiene note only.

### DEPS-DEPS-016 — auth0-js ^8.8.0 JWT validation bypass + open redirect
- **Original severity:** Critical — **Classification:** `potential_issue`
- **Reachability:** unreachable — **Persona:** n/a — **Confidence:** high
- **Validation:** Declared in `AdminPortal/package.json` but **never imported** anywhere in `AdminPortal/src` (auth is the cookie/form-based UserFront session; the SPA uses `localStorage` flags, not auth0). The JWT-bypass vector (CVE-2020-15084) has no code path. Dependency-hygiene issue; **downgraded** from Critical-exploitable. Same as ADMIN-SDE-004.

### DEPS-DEPS-017 — Angular 4 (all `@angular/*`) EOL framework
- **Original severity:** High — **Classification:** `potential_issue`
- **Reachability:** n/a — **Persona:** n/a — **Confidence:** medium
- **Validation:** ^4.0.0 confirmed (EOL ~2018). Missed sanitizer/template-compiler security fixes; no specific reachable exploit identified. Standing risk + upgrade blocker.

### DEPS-DEPS-018 — `@angular/http` ^4.0.0 deprecated/removed
- **Original severity:** Medium — **Classification:** `potential_issue` — **Confidence:** high
- **Validation:** Deprecated, removed in Angular 8. Unmaintained; upgrade blocker. No direct exploit.

### DEPS-DEPS-019 — core-js ^2.4.1 EOL/deprecated
- **Original severity:** Medium — **Classification:** `potential_issue` — **Confidence:** high
- **Validation:** 2.x deprecated/unmaintained. Hygiene.

### DEPS-DEPS-020 — rxjs ^5.1.0 / zone.js ^0.8.4 EOL
- **Original severity:** Medium — **Classification:** `potential_issue` — **Confidence:** high
- **Validation:** Both EOL, coupled to Angular 4. Hygiene/upgrade blocker.

### DEPS-DEPS-021 — `@angular/cli` 1.1.2 + legacy build toolchain
- **Original severity:** High (dev/build) — **Classification:** `potential_issue`
- **Reachability:** n/a (build-time) — **Persona:** n/a — **Confidence:** high
- **Validation:** 1.1.2 (2017) drags a deeply outdated webpack/dev-server tree with many advisories. Combined with no lockfile (DEPS-023), installed versions are nondeterministic. Build/supply-chain risk, not a runtime exploit.

### DEPS-DEPS-022 — Deprecated/EOL dev tooling (tslint, protractor, karma, ts 2.3, codelyzer)
- **Original severity:** Low (dev) — **Classification:** `potential_issue` — **Confidence:** high
- **Validation:** Sunset/abandoned dev tooling. No production exposure.

### DEPS-DEPS-023 — No dependency lockfile in AdminPortal
- **Original severity:** High (process/build integrity) — **Classification:** `potential_issue`
- **Reachability:** n/a — **Persona:** n/a — **Confidence:** high
- **Validation:** Confirmed: no `package-lock.json`/`yarn.lock`. Caret/tilde ranges resolve arbitrarily at install → non-reproducible builds, defeats `npm audit`, widens dependency-confusion/typosquat exposure. Real process gap.

---

## §3. ADMIN domain (24 candidates) — admin REST API (`/api/**`)

> Files: `UserResource.java` (`@RequestMapping("/api")`, class-level `@PreAuthorize("hasRole('ADMIN')")`), `AppointmentResource.java` (`/api/appointment`, same `@PreAuthorize`), `User.java`, AdminPortal SPA. The API is the Angular admin portal's backend.

### ADMIN-AUTHZ-001 — No URL-layer role enforcement for `/api/**`
- **Original severity:** High — **Classification:** `potential_issue`
- **Reachability:** conditionally_reachable — **Persona:** any_user (only if method security regresses) — **Confidence:** high
- **Validation:** `SecurityConfig` has no `antMatchers("/api/**").hasRole("ADMIN")`; `/api/**` falls under `anyRequest().authenticated()`. Admin enforcement rests **solely** on the class-level `@PreAuthorize`. Method security is active, so today a regular user gets 403 — but it is a single point of failure (one missing/typo'd annotation or disabled global-method-security ⇒ every customer can call admin APIs). Defense-in-depth gap.

### ADMIN-AUTHZ-002 — Class-level `@PreAuthorize` as the sole authz control
- **Original severity:** Medium — **Classification:** `potential_issue` — **Persona:** n/a — **Confidence:** high
- **Validation:** Same theme as -001; method-level granularity absent. Currently effective.

### ADMIN-AUTHZ-003 — CSRF-off + state-changing GET + credentialed CORS on admin API
- **Original severity:** High — **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** admin_only (victim must be a logged-in admin) — **Confidence:** high
- **Exploit path:** `/api/user/{username}/enable|disable` and `/api/appointment/{id}/confirm` use **bare** `@RequestMapping` (all HTTP methods incl. GET) and CSRF is disabled. An attacker lures a logged-in admin to a page with `<img src="https://app/api/user/attacker/enable">`; the admin's session executes the state change. No token, no POST required.
- **Notes:** Realizes against an admin victim (CSRF). See CHAIN-2.

### ADMIN-AUTHZ-004 — State-changing operations exposed over GET
- **Original severity:** Medium — **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** admin_only — **Confidence:** high
- **Validation:** `UserResource.java:45-53` (`/enable`, `/disable`) and `AppointmentResource` `/{id}/confirm` are bare `@RequestMapping` → GET-triggerable mutations. This is the property that makes ADMIN-AUTHZ-003 a one-click/zero-click CSRF.

### ADMIN-AUTHZ-005 — No per-object/tenant scoping (coarse ROLE_ADMIN)
- **Original severity:** Medium — **Classification:** `potential_issue`
- **Reachability:** reachable — **Persona:** admin_only — **Confidence:** high
- **Validation:** Any admin can act on **any** customer/object; no segmentation. Inherent to the single-role design; blast-radius concern.

### ADMIN-AUTHZ-006 — No audit logging of admin actions
- **Original severity:** Medium — **Classification:** `potential_issue` — **Persona:** admin_only — **Confidence:** high
- **Validation:** No audit trail beyond stray `System.out`. Detection/forensics gap.

### ADMIN-AUTHZ-007 — Credentialed CORS via manual filter despite `cors().disable()`
- **Original severity:** Medium — **Classification:** `potential_issue`
- **Reachability:** conditionally_reachable — **Persona:** n/a — **Confidence:** high
- **Validation:** `RequestFilter` sets ACAO `http://localhost:4200` + ACAC `true` on API responses. Fixed origin ⇒ not exploitable from an arbitrary site as written; fragile (see CONFIG-MISCONFIG-002).

### ADMIN-IDOR-001 — `/api/user/primary/transaction?username=` returns any user's transactions
- **Original severity:** High — **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** admin_only — **Confidence:** high
- **Exploit path:** `UserResource.java:35-38` → `transactionService.findPrimaryTransactionList(username)` with **no ownership/scope check**. `GET /api/user/primary/transaction?username=<victim>` returns any customer's primary-account transaction history. Unbounded across all customers (enumerated via ADMIN-IDOR-005). Within the admin role this is intended breadth, but there is zero object-level authz.

### ADMIN-IDOR-002 — `/api/user/savings/transaction?username=` returns any user's savings transactions
- **Original severity:** High — **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** admin_only — **Confidence:** high
- **Exploit path:** `UserResource.java:40-43`, same pattern as IDOR-001 for savings transactions.

### ADMIN-IDOR-003 — `/api/user/{username}/enable|disable` toggles any account
- **Original severity:** High — **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** admin_only — **Confidence:** high
- **Exploit path:** `UserResource.java:45-53` → `userService.enableUser/disableUser(username)`, no null check, any username, any HTTP method. Account-state DoS (disable victims) or re-enable of disabled accounts.

### ADMIN-IDOR-004 — `/api/appointment/{id}/confirm` confirms any appointment + NPE on bad id
- **Original severity:** Medium — **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** admin_only — **Confidence:** medium
- **Exploit path:** `AppointmentResource:29-31` → `confirmAppointment(id)` → `findOne(id)` (returns `null` if absent in Spring Data 1.x) then `setConfirmed(true)` with **no null check** (`AppointmentServiceImpl:30-33`). Confirms any appointment by id; a non-existent id throws NPE → 500 (minor DoS / error oracle).

### ADMIN-IDOR-005 — `/api/user/all` as an enumeration oracle
- **Original severity:** Medium — **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** admin_only — **Confidence:** high
- **Exploit path:** `UserResource.java:30-33` returns every user; the `username` values feed the username-keyed IDOR endpoints (IDOR-001/002/003). Also the source of the password-hash leak (ADMIN-SDE-001).

### ADMIN-PRIVESC-001 — Admins can disable peer admins
- **Original severity:** High — **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** admin_only — **Confidence:** high
- **Validation:** `disableUser(username)` has no self/peer-admin protection; one admin can disable another admin or themselves (account-state manipulation among privileged users).

### ADMIN-PRIVESC-002 — Mass-assignment via `@ModelAttribute User` on signup
- **Original severity:** High — **Classification:** `likely_exploitable`
- **Reachability:** reachable — **Persona:** any_user (unauthenticated — `/signup` is public) — **Confidence:** medium
- **Exploit path:** Signup binds the entire `User` entity from form fields. **Correction:** `createUser` hardcodes `ROLE_USER` and ignores any submitted role, so **privilege escalation to ADMIN via signup is a false positive**. The plausible-but-needs-runtime risk is integrity: `User.userId` is bindable, and `CrudRepository.save()` treats an entity with a set id as detached → a `merge`. Submitting `userId=<existing victim id>` with a new username could overwrite the victim's row (replacing username/password and resetting account references). This depends on Hibernate `AUTO`-id/merge behavior and the unique-email constraint, so it is `likely_exploitable` pending runtime verification — an account-overwrite/DoS primitive, not role escalation.
- **Notes:** Same candidate appears in AUTH; the role-escalation interpretation is rejected, the overwrite interpretation is flagged.

### ADMIN-PRIVESC-003 — Self-service re-enable via CSRF'd GET
- **Original severity:** Medium — **Classification:** `confirmed_exploitable` (sub-case of ADMIN-AUTHZ-003)
- **Reachability:** reachable — **Persona:** admin_only (victim) — **Confidence:** medium
- **Validation:** `GET /api/user/{username}/enable` + CSRF-off ⇒ a disabled-then-tricked admin (or any admin victim) re-enables an account via a forged GET.

### ADMIN-PRIVESC-004 — No step-up/re-auth for sensitive admin ops + rememberMe
- **Original severity:** Low — **Classification:** `potential_issue` — **Persona:** admin_only — **Confidence:** medium
- **Validation:** No re-authentication for destructive admin actions; combined with persistent rememberMe a stolen/long-lived session retains full power. Hardening gap.

### ADMIN-SDE-001 — `/api/user/all` leaks BCrypt password hashes
- **Original severity:** Critical — **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** admin_only — **Confidence:** high
- **Exploit path:** `UserResource.java:30-33` returns raw `User` JPA entities. `User.password` (`User.java:33`) has a **public getter and no `@JsonIgnore`** (only `appointmentList` and `userRoles` are annotated). So `GET /api/user/all` serializes every user's BCrypt hash. Combined with the static salt (CONFIG-CRYPTOFAIL-001) this is a directly crackable credential dump.
- **Notes:** Highest-impact confirmed finding. Fix with a DTO/projection or `@JsonIgnore` on `password`.

### ADMIN-SDE-002 — Over-exposure of PII + full account graph
- **Original severity:** High — **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** admin_only — **Confidence:** high
- **Validation:** The same raw-entity serialization exposes `email`, `phone`, `primaryAccount`, `savingsAccount` (balances/account numbers) and `recipientList`; none are `@JsonIgnore`d. Bulk PII/financial disclosure via `/api/user/all`.

### ADMIN-SDE-003 — Static BCrypt salt (crypto)
- **Original severity:** High — **Classification:** `confirmed_exploitable` (duplicate of CONFIG-CRYPTOFAIL-001)
- **Confidence:** high

### ADMIN-SDE-004 — Vulnerable auth0-js shipped in admin portal
- **Original severity:** High — **Classification:** `potential_issue` (duplicate of DEPS-016; declared-but-unused)
- **Confidence:** high

### ADMIN-SDE-005 — Admin creds/session over plaintext HTTP
- **Original severity:** High — **Classification:** `potential_issue`
- **Reachability:** conditionally_reachable — **Persona:** admin_only (network) — **Confidence:** medium
- **Validation:** AdminPortal calls the API over `http://` with `withCredentials:true`; no TLS enforcement (CONFIG-MISCONFIG-009). MITM on a non-TLS deployment can capture admin sessions. Deployment/network dependent.

### ADMIN-SDE-006 — `username` passed in query string (logged)
- **Original severity:** Low — **Classification:** `potential_issue` — **Persona:** admin_only — **Confidence:** high
- **Validation:** Transaction endpoints take `username` as a query param → lands in access logs/history. Minor info-leak/hygiene.

### ADMIN-SDE-007 — Debug output / stack traces from API path
- **Original severity:** Low — **Classification:** `potential_issue` (overlaps CONFIG-SDE-002/004)
- **Reachability:** unreachable (server logs) — **Persona:** system_only — **Confidence:** high

### ADMIN-SDE-008 — Client-side-only auth gate (localStorage) in AdminPortal
- **Original severity:** Low — **Classification:** `potential_issue`
- **Reachability:** n/a (client-side) — **Persona:** any_user — **Confidence:** high
- **Validation:** SPA gates views on a `localStorage` flag with no route guards; trivially bypassable client-side, but the **server** still enforces `@PreAuthorize`, so no server data is exposed by flipping the flag. Cosmetic/defense-in-depth.

---

## §4. AUTH domain (18 candidates) — authentication & registration

> `candidates.md` enumerates AUTH only by representative IDs + categorical descriptions (no per-ID rollup). Validated directly against source; derived/mapped IDs are labeled. Many AUTH items are the AUTH-domain view of root issues already validated in CONFIG/ADMIN.

### AUTH-AUTHZ-001 — Profile update IDOR (`POST /profile`)
- **Original severity:** High — **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** any_user — **Confidence:** high
- **Exploit path:** `UserController.java:31-45` — `profilePost` loads the target via `userService.findByUsername(newUser.getUsername())` (request-supplied), **not** from `Principal`, then overwrites `firstName/lastName/email/phone` and saves. `POST /profile` with `username=<victim>` edits **any** customer's profile. (Password/role/enabled are not touched, so it is PII/integrity tampering, not full account takeover — but `email` change can seed downstream recovery abuse.)

### AUTH-CRYPTOFAIL-001 — Static BCrypt salt seed
- **Original severity:** Critical — **Classification:** `confirmed_exploitable` (= CONFIG-CRYPTOFAIL-001) — **Confidence:** high

### AUTH-PRIVESC-001 — Mass-assignment on signup
- **Original severity:** High — **Classification:** `likely_exploitable` (= ADMIN-PRIVESC-002; role-escalation = false_positive, row-overwrite = likely) — **Confidence:** medium

### AUTH-MISCONFIG-001 — CSRF disabled (affects login/registration state changes)
- **Original severity:** High — **Classification:** `confirmed_exploitable` (= CONFIG-MISCONFIG-001) — **Confidence:** high

### AUTH-MISCONFIG-002 — Unauthenticated `/console` exposure
- **Original severity:** Critical — **Classification:** `false_positive` (= CONFIG-MISCONFIG-003; no console servlet) — **Confidence:** high

### AUTH-SDE-001 — Password hash exposure via API
- **Original severity:** Critical — **Classification:** `confirmed_exploitable` (= ADMIN-SDE-001) — **Confidence:** high

### AUTH (remaining ~11 representative items) — mapped to validated root issues
- **Classification:** mixed; see mapping. These cover: salt committed to source (= CONFIG-CRYPTOFAIL-002, `potential_issue`), `root` DB account (= CONFIG-CRYPTOFAIL-005, `potential_issue`), rememberMe default-key/persistent cookie (= CONFIG-MISCONFIG-005, `potential_issue`), plaintext transport for login (= CONFIG-MISCONFIG-009, `potential_issue`), EOL Spring Security session defaults (= DEPS-009, `potential_issue`), and verbose logging of auth flows (= CONFIG-MISCONFIG-007, `potential_issue`). No additional distinct exploit path beyond those root findings was identified.
- **Notes:** A genuine AUTH gap worth calling out: there is **no account-lockout/rate-limiting** on the form-login endpoint (no such control in `SecurityConfig`), enabling unthrottled credential stuffing/brute force — `potential_issue`, reachable, any_user (unauthenticated), confidence medium.

---

## §5. ACCT domain (14 candidates) — account management, deposit/withdraw

> Representative + categorical in `candidates.md`. Validated against `AccountController`, `AccountServiceImpl`. Derived IDs labeled.

### ACCT-DEPOSIT-001 — Unbounded, unfunded self-credit on deposit
- **Original severity:** (derived) High — **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** any_user — **Confidence:** high
- **Exploit path:** `AccountServiceImpl.java:58-80` `deposit(accountType, amount, principal)` resolves the account from `principal` (own account — no IDOR) then `accountBalance.add(new BigDecimal(amount))` and saves. There is **no funding source, no upper bound, and no positivity/limit check**. `POST /account/deposit` with `amount=1000000` credits the caller's own balance with money that does not exist. The caller can then `toSomeoneElse`/withdraw the fabricated funds.
- **Notes:** Core banking business-logic flaw. Reachability and persona are unambiguous; "needs runtime verification" only for end-to-end settlement, but the balance mutation itself is certain.

### ACCT-WITHDRAW-001 — No balance/overdraft check on withdraw
- **Original severity:** (derived) High — **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** any_user — **Confidence:** high
- **Exploit path:** `AccountServiceImpl.java:82-103` `withdraw()` does `accountBalance.subtract(new BigDecimal(amount))` with **no balance≥amount check** → drives the account negative (overdraft). With a **negative** `amount`, `subtract(negative)` becomes addition → self-credit (same primitive as deposit). Own account (principal-derived).

### ACCT-INPUT-001 — Unvalidated numeric amount parsing (`Double.parseDouble`)
- **Original severity:** (derived) Medium — **Classification:** `potential_issue`
- **Reachability:** reachable — **Persona:** any_user — **Confidence:** medium
- **Validation:** `amount` is parsed via `Double.parseDouble`/`new BigDecimal(double)`; non-numeric input throws `NumberFormatException` → unhandled 500 (minor DoS/error oracle). Float `BigDecimal(double)` also introduces precision artifacts in financial math. Robustness/validation gap.

### ACCT-INJ-001 — H2/console SQL via public console
- **Original severity:** Critical (conditional) — **Classification:** `false_positive` (= CONFIG-MISCONFIG-003; no console) — **Confidence:** high

### ACCT (remaining representative items) — mapped to validated root issues
- **Classification:** mixed. ACCT's IDOR/AUTHZ/INJ representatives overlap: recipient/account cross-user access (= XFER-IDOR-001, `confirmed_exploitable`), CSRF on deposit/withdraw (= CONFIG-MISCONFIG-001, `confirmed_exploitable`), and "no classic SQLi" — **validated negative**: account/transaction access uses Spring Data derived finders (parameterized), so SQL injection is `false_positive` here. No additional distinct exploit path identified.

---

## §6. XFER domain (22 candidates) — fund transfers & recipients

> Representative + categorical in `candidates.md`. Validated against `TransferController`, `TransactionServiceImpl`, `RecipientDao`. Derived IDs labeled.

### XFER-INJ-002 — Negative transfer amount → self-credit
- **Original severity:** Critical — **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** any_user — **Confidence:** high
- **Exploit path:** `TransactionServiceImpl.java:124-141` `toSomeoneElseTransfer` does `primaryAccount.setAccountBalance(balance.subtract(new BigDecimal(amount)))` with **no sign check**. `POST /transfer/toSomeoneElse` with `amount=-100000` ⇒ `subtract(-100000)` = **+100000** to the caller's own account. Self-credit of arbitrary funds. (`betweenAccountsTransfer` shares the unchecked-amount pattern, but only moves the victim's funds between their own primary/savings.)

### XFER-CSRF-004 — CSRF-forged transfer
- **Original severity:** Critical — **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** any_user (victim) — **Confidence:** high
- **Exploit path:** CSRF disabled (CONFIG-MISCONFIG-001) + `POST /transfer/toSomeoneElse` requires no token. Attacker page auto-submits a transfer using the victim's session. **Nuance:** `toSomeoneElseTransfer` only **debits** the sender (it does not credit an internal recipient account in this code), so the realized impact is forced victim fund loss/destruction rather than attacker crediting. Still a confirmed money-moving CSRF.

### XFER-IDOR-001 — Global recipient read/delete by name (`findRecipientByName` / `deleteRecipientByName`)
- **Original severity:** High — **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** any_user — **Confidence:** high
- **Exploit path:** `TransferController` `recipient/edit` and `recipient/delete` (both **GET**) call `transactionService.findRecipientByName(name)` / `deleteRecipientByName(name)`, backed by `RecipientDao.findByName(name)` which is **not scoped to the owning user**. `GET /transfer/recipient/edit?...name=<victim's recipient>` discloses another user's recipient PII incl. `accountNumber`, email, phone; `GET /transfer/recipient/delete?...` deletes another user's recipient. Cross-tenant IDOR (read + destructive) over GET.
- **Notes:** `toSomeoneElsePost` also resolves the recipient globally, but uses it only for the transaction description, so the cross-user reference there is low-impact; the edit/delete handlers are the real IDOR.

### XFER-AUTHZ/IDOR/CSRF (remaining representative items) — mapped/validated
- **Classification:** mixed. State-changing recipient operations over GET (= XFER-IDOR-001 property, `confirmed_exploitable`); CSRF across all transfer endpoints (= CONFIG-MISCONFIG-001/XFER-CSRF-004); "stored XSS in recipient/transfer fields" — **validated negative**: Thymeleaf auto-escaping is intact and `th:utext` is **absent** (grep-confirmed), so reflected/stored XSS via templates is `false_positive`; "SQLi in transfer params" — `false_positive` (Spring Data derived finders, parameterized). No additional distinct exploit path identified.

---

## §7. APPT domain (11 candidates) — appointments

> Representative + categorical in `candidates.md`. Validated against `AppointmentController`, `Appointment` entity, `AppointmentResource`, `AppointmentServiceImpl`. Derived IDs labeled.

### APPT-IDOR-004 — `/api/appointment/all` leaks raw `User` (password hashes + PII)
- **Original severity:** High — **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** admin_only — **Confidence:** high
- **Exploit path:** `AppointmentResource:22-24` returns `List<Appointment>`. `Appointment.user` (`Appointment.java:23-25`) is `@ManyToOne` with **no `@JsonIgnore`**, so each appointment serializes its full `User` — including the BCrypt `password` hash and account graph. `GET /api/appointment/all` is a second password-hash exfiltration sink (in addition to `/api/user/all`).

### APPT-MASSASSIGN-001 — Self-confirm appointment via mass-assignment (`confirmed=true`)
- **Original severity:** (derived) Medium — **Classification:** `confirmed_exploitable`
- **Reachability:** reachable — **Persona:** any_user — **Confidence:** medium
- **Exploit path:** `AppointmentController.java:39-53` binds `@ModelAttribute("appointment") Appointment` (which has `setConfirmed`). The create form does not expose `confirmed`, but an attacker adds `confirmed=true` to the `POST /appointment/create` body → the appointment is persisted pre-confirmed, bypassing the admin confirmation workflow (ADMIN `/api/appointment/{id}/confirm`).

### APPT-IDOR-001 — Appointment overwrite via mass-assigned `id`
- **Original severity:** High — **Classification:** `likely_exploitable`
- **Reachability:** reachable — **Persona:** any_user — **Confidence:** medium
- **Exploit path:** Same binding as above; `Appointment` has `setId`. Submitting `id=<existing appointment id>` to `POST /appointment/create` makes `appointmentService.createAppointment` call `save()` on an entity with a set id → JPA `merge`/update of that row. The handler sets `user=principal`, so a successful merge would **reassign and overwrite another user's appointment** (date/location/description) to the attacker. Depends on Hibernate `AUTO`-id merge semantics, hence `likely_exploitable` pending runtime confirmation.

### APPT-INPUT-001 — Lenient/again-unvalidated date parsing
- **Original severity:** (derived) Low — **Classification:** `potential_issue`
- **Reachability:** reachable — **Persona:** any_user — **Confidence:** medium
- **Validation:** `SimpleDateFormat("yyyy-MM-dd hh:mm").parse(date)` is lenient and uses 12-hour `hh`; malformed input throws `ParseException` → 500. Robustness gap; no security impact beyond minor DoS.

### APPT-AUTHZ-001 — Confirm-any-appointment over CSRF'd GET
- **Original severity:** Medium — **Classification:** `confirmed_exploitable` (= ADMIN-IDOR-004 / ADMIN-AUTHZ-003) — **Confidence:** medium

### APPT (remaining representative items) — mapped/validated
- **Classification:** mixed; overlap the above (IDOR via `/api/appointment/all`, mass-assignment, confirm-over-GET) plus CSRF (= CONFIG-MISCONFIG-001). "Stored XSS via appointment `location`/`description`" — **validated negative** (Thymeleaf auto-escape; no `th:utext`) → `false_positive`. No additional distinct exploit path identified.

---

## §8. Candidate → finding mapping & enumeration note

`candidates.md` is a **compiled summary** (counts = 131). It spells out per-ID candidates only for CONFIG (19), DEPS (23), ADMIN (24) = **66**, which are validated individually above. For AUTH (18), ACCT (14), XFER (22), APPT (11) = **65**, `candidates.md` provides representative IDs + categorical descriptions only (no per-ID rollup files exist on the candidates branch — only `domain-map.md` and `dependency-inventory.md`). Those 65 were validated **directly against the source**, and each categorical claim is mapped to a concrete validated finding (with derived IDs labeled). The write-ups above cover **85 individually-classified candidate entries**; because of heavy cross-listing these resolve to roughly **70 distinct root issues**.

> Note: the CONFIG rollup enumerates 20 detailed candidate IDs (MISCONFIG-001..009, CRYPTOFAIL-001..006, SDE-001..005) although its own summary count says 19 — its candidates are intentionally over-generated. All 20 IDs are validated above.

| Domain | Candidates (summary) | Disposition |
|---|---|---|
| CONFIG | 19 | Fully enumerated & validated (§1) |
| DEPS | 23 | Fully enumerated & validated (§2) |
| ADMIN | 24 | Fully enumerated & validated (§3) |
| AUTH | 18 | Representative IDs validated + categorical claims mapped to root findings (§4) |
| ACCT | 14 | Representative IDs validated + categorical claims mapped to root findings (§5) |
| XFER | 22 | Representative IDs validated + categorical claims mapped to root findings (§6) |
| APPT | 11 | Representative IDs validated + categorical claims mapped to root findings (§7) |

---

## §9. Cross-domain attack chains (validated)

### CHAIN-1 — Drive-by account drain (CSRF + unchecked amount)
`confirmed_exploitable`, any_user (victim). Victim logged into UserFront visits attacker page → auto-submitted `POST /transfer/toSomeoneElse` (no CSRF token, CONFIG-MISCONFIG-001) with attacker-chosen amount. Realized impact: forced debit of the victim's account (XFER-CSRF-004). The self-credit variant (XFER-INJ-002, ACCT-DEPOSIT-001) is attacker-driven on their own account and does not need a victim.

### CHAIN-2 — Admin credential dump → offline crack of all users
`confirmed_exploitable`, admin_only entry → all_users impact. Admin (or anyone who reaches `/api/**` if method-security regresses, ADMIN-AUTHZ-001) calls `GET /api/user/all` (ADMIN-SDE-001) or `GET /api/appointment/all` (APPT-IDOR-004) → password-hash dump. Static salt (CONFIG-CRYPTOFAIL-001) ⇒ precompute once, cluster equal hashes (CONFIG-SDE-005), crack offline → mass account takeover. With hardcoded `root` DB creds (CONFIG-CRYPTOFAIL-003) the same data is reachable directly if the DB is network-accessible.

### CHAIN-3 — Cross-tenant recipient harvest + tamper
`confirmed_exploitable`, any_user. `GET /transfer/recipient/edit` (XFER-IDOR-001) enumerates other users' recipients (names, emails, **account numbers**); `GET /transfer/recipient/delete` removes them — both unscoped, both GET (also CSRF-able).

### CHAIN-4 — Profile PII tamper → recovery/social-engineering seed
`confirmed_exploitable`, any_user. `POST /profile` (AUTH-AUTHZ-001) rewrites any user's `email`/`phone`; combined with no CSRF this can be victim-driven too. Sets up downstream account-recovery or support-desk abuse.

### CHAIN-5 — Fabricated balance → outbound transfer
`confirmed_exploitable` (balance mutation) / `likely_exploitable` (end-to-end value extraction), any_user. `POST /account/deposit` mints balance (ACCT-DEPOSIT-001) → `POST /transfer/toSomeoneElse` moves it out (XFER). Internal-settlement realization needs runtime confirmation, but the balance inflation is certain.

---

## §10. Methodology & limitations

- **Sources:** `candidates.md` (compiled summary) on `devin/1780592904-hunter-pipeline-candidates`; full per-ID rollups for CONFIG/DEPS/ADMIN; live source in `UserFront/` and `AdminPortal/`.
- **Backend dependency versions:** resolved with `mvn dependency:tree` (not just declared ranges). Frontend versions are declared ranges only — **no lockfile** exists (DEPS-023), so installed transitive versions are nondeterministic.
- **Verified negatives:** no `th:utext` in templates (Thymeleaf auto-escaping intact → template XSS not reachable); no Bean Validation usage (hibernate-validator EL-injection not reachable); no `@Query`/HQL (Hibernate SQLi not reachable); no H2/console servlet (`/console` 404 → console-SQL false positive); auth0-js declared but unused.
- **Corrections to candidate analysis:** signup mass-assignment cannot escalate to ADMIN (role hardcoded); rememberMe uses a random startup key, not a predictable static default; Spring Security 4.2.x does set frame-options/content-type-options by default; most DEPS "Critical RCE" vectors are not wired up in this app (classified `potential_issue`, not `confirmed_exploitable`).
- **Limitation:** classifications are from static analysis. Items marked `likely_exploitable` (ADMIN/AUTH-PRIVESC-002 row overwrite, APPT-IDOR-001 appointment overwrite, CHAIN-5 settlement) require runtime confirmation. This is a triage deliverable — no code was modified and no fixes were implemented.
