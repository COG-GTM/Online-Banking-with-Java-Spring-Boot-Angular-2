# DEPS Domain Rollup — Dependencies

- **Domain:** DEPS (Dependencies)
- **Repo:** COG-GTM/Online-Banking-with-Java-Spring-Boot-Angular-2
- **Stack:** Java 8 / Spring Boot 1.5.4.RELEASE / Angular 4 / MySQL
- **Scanned files:** `UserFront/pom.xml`, `AdminPortal/package.json`
- **Backend versions:** resolved via `mvn dependency:tree` (Spring Boot 1.5.4 parent BOM)
- **Frontend versions:** declared SemVer ranges in `package.json` (⚠️ no `package-lock.json` / `yarn.lock` present — transitive versions are unpinned)
- **Vuln class:** DEPS (vulnerable / EOL / unmaintained dependencies)
- **Scan date:** 2026-06-04

> Scanner output only. This is a candidate catalog for triage — no code was changed. CVSS values are the commonly published CVE base scores; confirm exploitability in context before remediation.

---

## 1. Dependency Catalog

### 1.1 Backend — production scope (`UserFront/pom.xml`, resolved)

| Dependency | Resolved version | Source | EOL? | CVE? |
|---|---|---|---|---|
| org.springframework.boot:spring-boot-* (starter-web, -thymeleaf, -jdbc, -data-jpa, -security, autoconfigure, -logging, -aop, -tomcat) | 1.5.4.RELEASE | direct + BOM | **EOL (Aug 2019)** | umbrella |
| com.fasterxml.jackson.core:jackson-databind | 2.8.8 | transitive (starter-web) | EOL | **Yes — many** |
| com.fasterxml.jackson.core:jackson-core | 2.8.8 | transitive | EOL | — |
| com.fasterxml.jackson.core:jackson-annotations | 2.8.0 | transitive | EOL | — |
| org.springframework:spring-{web,webmvc,beans,context,expression,jdbc,tx,orm,aop,aspects,core} | 4.3.9.RELEASE | transitive | **EOL** | **Yes** |
| org.springframework.security:spring-security-{config,core,web} | 4.2.3.RELEASE | direct (starter-security) | **EOL** | **Yes** |
| org.springframework.data:spring-data-jpa | 1.11.4.RELEASE | transitive (starter-data-jpa) | EOL | — |
| org.springframework.data:spring-data-commons | 1.13.4.RELEASE | transitive | EOL | **Yes (RCE)** |
| org.hibernate:hibernate-core | 5.0.12.Final | transitive (data-jpa) | EOL | **Yes (SQLi)** |
| org.hibernate:hibernate-entitymanager | 5.0.12.Final | transitive | EOL | — |
| org.hibernate:hibernate-validator | 5.3.5.Final | transitive (starter-web) | EOL | **Yes** |
| org.apache.tomcat.embed:tomcat-embed-{core,el,websocket} | 8.5.15 | transitive (starter-tomcat) | **EOL** | **Yes — many** |
| org.apache.tomcat:tomcat-jdbc / tomcat-juli | 8.5.15 | transitive (starter-jdbc) | EOL | — |
| mysql:mysql-connector-java | 5.1.42 | direct | **EOL (5.1.x)** | **Yes** |
| org.yaml:snakeyaml | 1.17 | transitive (starter) | EOL | **Yes (RCE)** |
| ch.qos.logback:logback-classic / logback-core | 1.1.11 | transitive (starter-logging) | EOL | **Yes (JNDI)** |
| org.slf4j:slf4j-api / jul-to-slf4j / log4j-over-slf4j / jcl-over-slf4j | 1.7.25 | transitive | old | — |
| org.thymeleaf:thymeleaf / thymeleaf-spring4 | 2.1.5.RELEASE | direct (starter-thymeleaf) | **EOL** | EOL risk |
| ognl:ognl | 3.0.8 | transitive (thymeleaf) | EOL | EOL risk |
| org.codehaus.groovy:groovy | 2.4.11 | transitive (thymeleaf-layout-dialect) | EOL | **Yes** |
| nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect | 1.4.0 | transitive | old | — |
| org.aspectj:aspectjweaver | 1.8.10 | transitive (starter-aop) | old | — |
| org.javassist:javassist | 3.21.0-GA | transitive (hibernate-core) | old | — |
| org.unbescape:unbescape | 1.1.0.RELEASE | transitive | old | — |
| antlr:antlr | 2.7.7 | transitive (hibernate) | EOL | — |
| dom4j:dom4j | 1.6.1 | transitive (hibernate) | **EOL** | XXE risk |

### 1.2 Backend — test scope (not in production runtime)

| Dependency | Version | CVE? |
|---|---|---|
| org.springframework.boot:spring-boot-starter-test | 1.5.4.RELEASE | EOL |
| com.jayway.jsonpath:json-path | 2.2.0 | **Yes (DoS)** |
| net.minidev:json-smart | 2.2.1 | **Yes** |
| net.minidev:accessors-smart / org.ow2.asm:asm | 1.1 / 5.0.3 | old |
| junit:junit | 4.12 | **Yes (info disclosure)** |
| org.mockito:mockito-core | 1.10.19 | EOL |
| org.assertj:assertj-core | 2.6.0 | old |
| org.skyscreamer:jsonassert | 1.4.0 | old |
| org.hamcrest:hamcrest-core / -library | 1.3 | old |

### 1.3 Frontend — production scope (`AdminPortal/package.json`)

| Dependency | Declared range | EOL? | CVE? |
|---|---|---|---|
| @angular/animations, common, compiler, core, forms, platform-browser, platform-browser-dynamic, router | ^4.0.0 | **EOL** | **Yes** |
| @angular/http | ^4.0.0 | **Deprecated/removed (Angular 8)** | unmaintained |
| auth0-js | ^8.8.0 | EOL line | **Yes — CVSS 9.8** |
| core-js | ^2.4.1 | **EOL/deprecated** | unmaintained |
| rxjs | ^5.1.0 | **EOL** | unmaintained |
| zone.js | ^0.8.4 | EOL | unmaintained |

### 1.4 Frontend — dev scope (`AdminPortal/package.json`)

| Dependency | Declared range | Notes |
|---|---|---|
| @angular/cli | 1.1.2 | Ancient; pulls vulnerable build toolchain (webpack-dev-server etc.) |
| @angular/compiler-cli, language-service | ^4.0.0 | EOL |
| typescript | ~2.3.3 | EOL |
| codelyzer | ~3.0.1 | EOL |
| tslint | ~5.3.2 | **Deprecated (project sunset)** |
| protractor | ~5.1.2 | **Deprecated/EOL** |
| karma / karma-* | ~1.7.0 / various | EOL |
| jasmine-core / @types/jasmine | ~2.6.2 / 2.5.45 | old |
| ts-node | ~3.0.4 | old |
| @types/node | ~6.0.60 | old |

---

## 2. Vulnerability Candidates

ID format: `DEPS-DEPS-{NNN}`. Severity reflects worst published CVE for the affected version in this domain.

### DEPS-DEPS-001 — jackson-databind 2.8.8: polymorphic deserialization RCE
- **Severity:** Critical
- **Affected:** `com.fasterxml.jackson.core:jackson-databind:2.8.8` (transitive via `spring-boot-starter-web`)
- **CVEs:** CVE-2017-7525, CVE-2017-15095, CVE-2017-17485, CVE-2018-7489, CVE-2018-14718–14721, CVE-2019-12086, CVE-2019-14379, CVE-2019-14439, CVE-2020-36518 (and the broader "deserialization gadget" series)
- **Description:** 2.8.8 predates the blocklist/`activateDefaultTyping` hardening. With polymorphic typing enabled (directly or via a gadget on the classpath), attacker-controlled JSON can instantiate gadget classes leading to remote code execution (multiple CVEs CVSS 9.8) and DoS (CVE-2020-36518). One of the highest-risk libraries on the classpath.
- **Fix direction:** Upgrade to a patched 2.12.7.1+ / 2.13.4.2+ line (comes free with Spring Boot 3).

### DEPS-DEPS-002 — Spring Framework 4.3.9.RELEASE: SpEL RCE + multiple
- **Severity:** Critical
- **Affected:** `org.springframework:spring-* :4.3.9.RELEASE` (web, webmvc, beans, context, expression, etc.)
- **CVEs:** CVE-2018-1270 (SpEL RCE via STOMP/WebSocket, CVSS 9.8), CVE-2018-1271, CVE-2018-1272, CVE-2018-11039, CVE-2018-11040, CVE-2020-5398, CVE-2020-5421. **Spring4Shell adjacency:** CVE-2022-22965 — see note below.
- **Description:** Spring Framework 4.3.x is end-of-life and unpatched. CVE-2018-1270 allows RCE through crafted SpEL in messaging. Reflected file download and CORS/JSONP issues also apply.
- **Spring4Shell note:** CVE-2022-22965's canonical exploit targets Spring 5.2/5.3 on Tomcat WAR deployments with JDK 9+. This app is an executable JAR on **Java 8** with embedded Tomcat, so the classic RDF/AccessLogValve path is largely mitigated — but the framework is EOL and will never receive the fix, so it remains "RCE-adjacent."
- **Fix direction:** Migrate to Spring Framework 6.x via Spring Boot 3.

### DEPS-DEPS-003 — Spring Data Commons 1.13.4.RELEASE: SpEL injection RCE
- **Severity:** Critical
- **Affected:** `org.springframework.data:spring-data-commons:1.13.4.RELEASE` (transitive via `spring-boot-starter-data-jpa`)
- **CVEs:** CVE-2018-1273 (property-path SpEL injection RCE, CVSS 9.8; affects 1.13.0–1.13.10), CVE-2018-1259 (XXE with Spring Data + XMLBeam)
- **Description:** Request-bound property paths are evaluated as SpEL, allowing unauthenticated remote code execution when projection/binding is exposed. Directly applicable to JPA repositories bound to web request parameters.
- **Fix direction:** Upgrade to 1.13.11+ / 2.0.6+ (via Spring Boot 3).

### DEPS-DEPS-004 — Apache Tomcat (embedded) 8.5.15: Ghostcat + RCE cluster
- **Severity:** Critical
- **Affected:** `org.apache.tomcat.embed:tomcat-embed-{core,el,websocket}:8.5.15`, `org.apache.tomcat:tomcat-jdbc:8.5.15`
- **CVEs:** CVE-2020-1938 (Ghostcat AJP file read/inclusion → RCE, CVSS 9.8), CVE-2020-9484 (session-persistence deserialization RCE), CVE-2017-12617 (JSP upload RCE), CVE-2019-0232 (CGI RCE on Windows), CVE-2018-11784 (open redirect), CVE-2020-1935 (request smuggling), CVE-2021-25122, CVE-2021-25329
- **Description:** Tomcat 8.5.15 (2017) is many dozens of patch releases behind and end-of-life. Exposure depends on enabled connectors (AJP for Ghostcat) and config, but the breadth and severity warrant an immediate upgrade.
- **Fix direction:** Upgrade embedded Tomcat to a supported 9.0.x/10.1.x line (bundled with newer Spring Boot).

### DEPS-DEPS-005 — SnakeYAML 1.17: deserialization RCE + DoS
- **Severity:** Critical
- **Affected:** `org.yaml:snakeyaml:1.17` (transitive via `spring-boot-starter`)
- **CVEs:** CVE-2022-1471 (unsafe `Constructor` deserialization RCE, CVSS 9.8; fixed 2.0), CVE-2022-25857 (DoS), CVE-2017-18640 (Billion Laughs entity expansion; fixed 1.26), CVE-2022-38749/38750/38751/38752 (stack-overflow DoS)
- **Description:** When YAML is parsed with the default `Constructor`, malicious documents can instantiate arbitrary types → RCE. Spring Boot loads YAML config via SnakeYAML; risk rises if any user/externally controlled YAML is parsed.
- **Fix direction:** Upgrade SnakeYAML to 2.0+; prefer `SafeConstructor` for untrusted input.

### DEPS-DEPS-006 — Spring Boot 1.5.4.RELEASE: end-of-life BOM (umbrella)
- **Severity:** High
- **Affected:** `org.springframework.boot:*:1.5.4.RELEASE` (parent + all starters)
- **CVEs:** Umbrella — pins the entire vulnerable managed dependency set above; Spring Boot 1.x reached EOL in **August 2019** and receives no security patches.
- **Description:** The parent BOM is the root cause for most of these candidates: it freezes Jackson, Tomcat, Spring, Spring Security, Hibernate, SnakeYAML and Logback at vulnerable versions. Remediation of individual libraries is brittle while pinned to this BOM.
- **Fix direction:** Migrate to Spring Boot 3.x (Java 17/21), which transitively resolves the bulk of these findings.

### DEPS-DEPS-007 — Hibernate ORM 5.0.12.Final: SQL injection
- **Severity:** High
- **Affected:** `org.hibernate:hibernate-core:5.0.12.Final`, `hibernate-entitymanager:5.0.12.Final`
- **CVEs:** CVE-2019-14900 (HQL literal SQL injection), CVE-2020-25638 (SQLi when `hibernate.use_sql_comments` enabled)
- **Description:** Improper escaping of literals in HQL→SQL translation allows injection in affected versions (before 5.3.20/5.4.18).
- **Fix direction:** Upgrade to 5.4.24+ / 5.6.x (via Spring Boot 3 → 6.x).

### DEPS-DEPS-008 — Hibernate Validator 5.3.5.Final: EL injection / privilege escalation
- **Severity:** High
- **Affected:** `org.hibernate:hibernate-validator:5.3.5.Final` (transitive via `spring-boot-starter-web`)
- **CVEs:** CVE-2017-7536 (interpolated constraint-violation messages evaluated as EL → privilege escalation / info disclosure; fixed 5.3.6 / 5.4.1)
- **Description:** Attacker-influenced data placed in validation messages can be EL-evaluated, exposing internal state or escalating privileges.
- **Fix direction:** Upgrade to 6.x.

### DEPS-DEPS-009 — Spring Security 4.2.3.RELEASE: privilege escalation / bypass
- **Severity:** High
- **Affected:** `org.springframework.security:spring-security-{config,core,web}:4.2.3.RELEASE`
- **CVEs:** CVE-2018-1258 (method-security privilege escalation when combined with Spring Framework 5.0.5; CVSS 8.8), CVE-2018-1199 (security-bypass for static resources), CVE-2019-11272 (`StandardPasswordEncoder`/`BCrypt` related). CVE-2022-22978 (`RegexRequestMatcher` authz bypass) primarily affects 5.4–5.6, listed by pre-scan — flagged as adjacency only.
- **Description:** EOL Spring Security line with known authorization-bypass / privilege-escalation issues; no further patches.
- **Fix direction:** Upgrade with Spring Boot 3 → Spring Security 6.x.

### DEPS-DEPS-010 — MySQL Connector/J 5.1.42: deserialization / EOL driver
- **Severity:** High
- **Affected:** `mysql:mysql-connector-java:5.1.42` (direct)
- **CVEs (5.1.x):** CVE-2017-3523, CVE-2017-3586, CVE-2018-3258 (deserialization of server-supplied data), CVE-2019-2692
- **Description:** Connector/J 5.1.x is unsupported. A malicious/compromised MySQL server (or MITM) can exploit deserialization/handshake flaws. **Note:** the pre-scan CVEs **CVE-2023-21971** and **CVE-2021-2471 (XXE)** apply to Connector/J **8.0.x** (≤ 8.0.27/8.0.32), not to the resolved 5.1.42 — but 5.1.42 is itself EOL with its own CVE set, so the finding stands.
- **Fix direction:** Migrate to `com.mysql:mysql-connector-j` 8.4+.

### DEPS-DEPS-011 — Logback 1.1.11: JNDI lookup RCE
- **Severity:** High
- **Affected:** `ch.qos.logback:logback-classic:1.1.11`, `logback-core:1.1.11` (transitive via `spring-boot-starter-logging`)
- **CVEs:** CVE-2021-42550 (config-receiver JNDI/RCE; affects < 1.2.9)
- **Description:** An attacker able to edit/inject logback configuration can trigger JNDI lookups leading to RCE. 1.1.11 is well below the 1.2.9 fix.
- **Fix direction:** Upgrade to 1.2.13+ / 1.4.x.

### DEPS-DEPS-012 — Apache Groovy 2.4.11: insecure temp-dir info disclosure
- **Severity:** Medium
- **Affected:** `org.codehaus.groovy:groovy:2.4.11` (transitive via `thymeleaf-layout-dialect`)
- **CVEs:** CVE-2020-17521 (sensitive data written to world-readable temp directory; affects 2.0–2.4.20)
- **Fix direction:** Upgrade Groovy to 2.4.21+ / 3.0.7+ (typically via thymeleaf upgrade).

### DEPS-DEPS-013 — Thymeleaf 2.1.5.RELEASE: EOL template engine (SSTI surface)
- **Severity:** Medium
- **Affected:** `org.thymeleaf:thymeleaf:2.1.5.RELEASE`, `thymeleaf-spring4:2.1.5.RELEASE` (direct)
- **Description:** Thymeleaf 2.1.x is end-of-life and unmaintained. Later majors hardened against server-side template injection (expression-evaluation) issues; an EOL engine will not receive these fixes. Risk is elevated wherever fragment/expression values are influenced by user input.
- **Fix direction:** Upgrade to Thymeleaf 3.1.x (Spring Boot 3 default).

### DEPS-DEPS-014 — OGNL 3.0.8: EOL expression-language library
- **Severity:** Medium
- **Affected:** `ognl:ognl:3.0.8` (transitive via Thymeleaf)
- **Description:** OGNL is the class of library behind numerous expression-injection RCEs (Struts-style). 3.0.8 is EOL; remediated implicitly by upgrading the Thymeleaf chain that pulls it.
- **Fix direction:** Remove/upgrade via Thymeleaf 3.x.

### DEPS-DEPS-015 — Test-scope libraries: json-smart / json-path / JUnit
- **Severity:** Medium (test scope — not in production runtime)
- **Affected:** `net.minidev:json-smart:2.2.1`, `com.jayway.jsonpath:json-path:2.2.0`, `junit:junit:4.12`
- **CVEs:** CVE-2021-27568 (json-smart uncontrolled exception), CVE-2023-1370 (json-smart/json-path deep-nesting DoS), CVE-2020-15250 (JUnit `TemporaryFolder` local info disclosure)
- **Description:** Pulled only via `spring-boot-starter-test`; no production exposure, but flagged for completeness and CI hygiene.
- **Fix direction:** Resolved by upgrading the test BOM (Spring Boot 3).

### DEPS-DEPS-016 — auth0-js ^8.8.0: JWT validation bypass + open redirect
- **Severity:** Critical
- **Affected:** `auth0-js@^8.8.0` (`AdminPortal/package.json`, production)
- **CVEs:** CVE-2020-15084 (improper audience/expiration validation → authentication bypass, CVSS 9.8; fixed 9.3.0), CVE-2021-43812 (open redirect via `redirectTo`; fixed 9.16.2)
- **Description:** Affected versions fail to properly validate the JWT `audience`/expiration, allowing forged or replayed tokens to be accepted — a direct authentication-bypass risk for an admin banking portal. Open-redirect aids phishing/token theft.
- **Fix direction:** Upgrade auth0-js to ≥ 9.16.2 (or migrate to auth0-spa-js).

### DEPS-DEPS-017 — Angular 4.0.0 (all @angular/* packages): EOL framework
- **Severity:** High
- **Affected:** `@angular/{animations,common,compiler,core,forms,platform-browser,platform-browser-dynamic,router}@^4.0.0` (production)
- **Description:** Angular 4 has been unsupported since ~2018. Later releases shipped DomSanitizer/XSS and template-compiler security fixes that 4.x never received. An EOL frontend framework on a banking admin portal is a standing risk and a blocker to patching the rest of the JS tree.
- **Fix direction:** Migrate to a supported Angular LTS (17+). A migration branch (`devin/...-angular-upgrade-v4-to-v21`) already exists in this repo.

### DEPS-DEPS-018 — @angular/http ^4.0.0: deprecated/removed module
- **Severity:** Medium
- **Affected:** `@angular/http@^4.0.0` (production)
- **Description:** `@angular/http` was deprecated and **removed in Angular 8**, replaced by `@angular/common/http` (`HttpClient`). It receives no security maintenance and blocks framework upgrades.
- **Fix direction:** Replace with `HttpClient` as part of the Angular upgrade.

### DEPS-DEPS-019 — core-js ^2.4.1: EOL / deprecated
- **Severity:** Medium
- **Affected:** `core-js@^2.4.1` (production)
- **Description:** core-js 2.x is deprecated/unmaintained (the maintainer explicitly flags it as obsolete via postinstall). No security or correctness fixes.
- **Fix direction:** Upgrade to core-js 3.x (driven by the Angular/CLI upgrade).

### DEPS-DEPS-020 — rxjs ^5.1.0 / zone.js ^0.8.4: EOL runtime libraries
- **Severity:** Medium
- **Affected:** `rxjs@^5.1.0`, `zone.js@^0.8.4` (production)
- **Description:** Both are EOL and tightly coupled to Angular 4. Superseded by RxJS 6/7 and modern zone.js; no maintenance.
- **Fix direction:** Upgrade alongside the Angular framework migration.

### DEPS-DEPS-021 — @angular/cli 1.1.2 + legacy build toolchain: transitive supply-chain risk
- **Severity:** High (dev/build scope)
- **Affected:** `@angular/cli@1.1.2` (and the webpack/build chain it pulls)
- **Description:** Angular CLI 1.1.2 (2017) drags in a large, deeply outdated transitive tree (webpack, webpack-dev-server, loaders) with numerous published advisories. Combined with the missing lockfile (DEPS-DEPS-023), the actual installed versions are nondeterministic and likely vulnerable.
- **Fix direction:** Upgrade to a current Angular CLI; pin via lockfile; run `npm audit` in CI.

### DEPS-DEPS-022 — Deprecated/EOL dev tooling: tslint, protractor, karma, typescript 2.3, codelyzer
- **Severity:** Low (dev scope)
- **Affected:** `tslint@~5.3.2` (deprecated), `protractor@~5.1.2` (EOL), `karma@~1.7.0` (EOL), `typescript@~2.3.3` (EOL), `codelyzer@~3.0.1` (EOL), `@angular/{compiler-cli,language-service}@^4.0.0`
- **Description:** Sunset/abandoned developer tooling. Low direct production risk, but unmaintained and blocks toolchain modernization.
- **Fix direction:** Migrate to ESLint (`angular-eslint`), modern Karma/Jasmine or Jest, current TypeScript — part of the Angular upgrade.

### DEPS-DEPS-023 — No dependency lockfile in AdminPortal: non-reproducible / supply-chain exposure
- **Severity:** High (process/build integrity)
- **Affected:** `AdminPortal/` — no `package-lock.json` or `yarn.lock`
- **Description:** With only caret/tilde ranges and no lockfile, transitive dependencies resolve to arbitrary newer versions at install time. This breaks reproducibility, defeats `npm audit` triage, and widens exposure to typosquatting / dependency-confusion / compromised-release supply-chain attacks.
- **Fix direction:** Commit a lockfile, enable `npm ci` in CI, add `npm audit --audit-level=high` (or Snyk/Dependabot) gating.

---

## 3. Domain Rollup Summary

**Total candidates:** 23 (`DEPS-DEPS-001` … `DEPS-DEPS-023`)

| Severity | Count | IDs |
|---|---|---|
| Critical | 6 | 001 (jackson-databind), 002 (spring-framework), 003 (spring-data-commons), 004 (tomcat), 005 (snakeyaml), 016 (auth0-js) |
| High | 8 | 006 (spring-boot EOL BOM), 007 (hibernate), 008 (hibernate-validator), 009 (spring-security), 010 (mysql-connector), 011 (logback), 017 (angular EOL), 021 (angular-cli toolchain), 023 (no lockfile) |
| Medium | 7 | 012 (groovy), 013 (thymeleaf), 014 (ognl), 015 (test-scope libs), 018 (@angular/http), 019 (core-js), 020 (rxjs/zone.js) |
| Low | 1 | 022 (dev tooling) |

> Counts: High row lists 9 IDs — 006, 007, 008, 009, 010, 011, 017, 021, 023.

### Highest-leverage remediations
1. **Migrate UserFront to Spring Boot 3.x (Java 17/21).** Single change that transitively retires DEPS-DEPS-001..011 (jackson-databind, spring-framework, spring-data-commons, tomcat, snakeyaml, hibernate, hibernate-validator, spring-security, logback). Migration branches already exist in this repo (`devin/...-spring-boot-3-*`).
2. **Upgrade auth0-js to ≥ 9.16.2 (DEPS-DEPS-016).** Smallest change with the highest direct auth-bypass impact; can ship independently of the full Angular upgrade.
3. **Migrate AdminPortal to a supported Angular LTS + commit a lockfile (DEPS-DEPS-017..023).** Retires the EOL frontend tree and restores build reproducibility. Branch `devin/...-angular-upgrade-v4-to-v21` exists.
4. **Add CI dependency gating:** OWASP Dependency-Check / Snyk for Maven; `npm audit` / Snyk / Dependabot for npm — to prevent regression.

### Caveats / verification notes
- Backend versions are authoritative (resolved via `mvn dependency:tree`). Frontend versions are **declared ranges only** — install + `npm ls` / `npm audit` would pin exact transitive versions (blocked by the missing lockfile, DEPS-DEPS-023).
- CVSS scores are published CVE base scores; runtime exploitability depends on configuration (e.g., Tomcat AJP enabled for Ghostcat, polymorphic typing enabled for jackson-databind, untrusted-YAML parsing for SnakeYAML).
- Spring4Shell (CVE-2022-22965) and several Spring Security CVEs are listed as **adjacency** items: they primarily target Spring 5.x, but the EOL 4.x line here will never be patched.
