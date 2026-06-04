# Dependency Inventory — Online Banking

## Backend (UserFront — Maven/Spring Boot 1.5.4)

| Package | Version | Direct/Transitive | Known CVEs | Severity | Used by domain(s) | Notes |
|---------|---------|-------------------|------------|----------|--------------------|-------|
| spring-boot-starter-parent | 1.5.4.RELEASE | Direct (parent) | Multiple (EOL since 2019) | Critical | ALL | End-of-life; no security patches since Aug 2019 |
| spring-boot-starter-web | 1.5.4 (inherited) | Direct | CVE-2018-1270, CVE-2018-1271, CVE-2022-22965 (Spring4Shell) | Critical | ALL | Embedded Tomcat, Spring MVC |
| spring-boot-starter-security | 1.5.4 (inherited) | Direct | CVE-2018-1199, CVE-2022-22978 | High | AUTH, ADMIN | Spring Security 4.2.x |
| spring-boot-starter-data-jpa | 1.5.4 (inherited) | Direct | Inherited from Hibernate | Medium | ACCT, XFER, APPT | Hibernate ORM |
| spring-boot-starter-thymeleaf | 1.5.4 (inherited) | Direct | CVE-2023-38286 (SSTI) | High | AUTH, ACCT, XFER | Template engine |
| spring-boot-starter-jdbc | 1.5.4 (inherited) | Direct | N/A | Low | ALL | JDBC auto-config |
| mysql-connector-java | 5.1.x (inherited) | Direct | CVE-2021-2471, CVE-2022-21363 | High | ALL | MySQL JDBC driver |
| spring-boot-starter-test | 1.5.4 (inherited) | Direct (test) | N/A | N/A | N/A | Test scope only |

## Frontend (AdminPortal — npm/Angular 4)

| Package | Version | Direct/Transitive | Known CVEs | Severity | Used by domain(s) | Notes |
|---------|---------|-------------------|------------|----------|--------------------|-------|
| @angular/core | ^4.0.0 | Direct | Multiple (EOL) | High | ADMIN | End-of-life Angular version |
| @angular/http | ^4.0.0 | Direct | Deprecated module | Medium | ADMIN | Replaced by HttpClient in Angular 5+ |
| auth0-js | ^8.8.0 | Direct | CVE-2020-15084, CVE-2021-43812 | High | ADMIN | Auth0 SDK — unclear if actually used |
| rxjs | ^5.1.0 | Direct | N/A | Low | ADMIN | Reactive extensions |
| zone.js | ^0.8.4 | Direct | N/A | Low | ADMIN | Angular zone.js |
| typescript | ~2.3.3 | Dev | N/A | N/A | ADMIN | Build-time only |
| karma | ~1.7.0 | Dev | CVE-2020-11023 (transitive jQuery) | Medium | N/A | Test runner only |
| protractor | ~5.1.2 | Dev | Deprecated | Low | N/A | E2E test framework (deprecated) |

## Summary

- **Critical risk:** Spring Boot 1.5.4 is massively out of support (EOL 2019). Known RCEs (Spring4Shell), auth bypasses, and SSTI vulnerabilities affect this version.
- **High risk:** Angular 4 is EOL. auth0-js has known vulnerabilities. mysql-connector-java has known SQLi/RCE vectors.
- **The entire stack is circa 2017 with no security updates applied.**
