---
name: testing-userfront
description: Boot and test the UserFront module (Java 17 / Spring Boot 3.2.5) end-to-end against a local MySQL. Use when verifying UserFront auth, signup, or dashboard changes at runtime.
---

# Testing UserFront end-to-end

UserFront is a Spring Boot app (after migration: Java 17 + Spring Boot 3.2.5 + Spring Security 6 + Hibernate 6) that talks to a MySQL `onlinebanking` database. It serves a Thymeleaf UI on `http://localhost:8080`.

## Prerequisites

### 1. MySQL (Docker)
```bash
docker run -d --name obank-mysql -e MYSQL_ROOT_PASSWORD=avengers1993 -e MYSQL_DATABASE=onlinebanking -p 3306:3306 mysql:8
```
Match the credentials in `UserFront/src/main/resources/application.properties` (`spring.datasource.username`/`password`). The app uses `ddl-auto=update`, so tables are auto-created on first boot.

### 2. Seed the role table (REQUIRED for signup)
Signup calls `roleDao.findByName("ROLE_USER")` and then `roleDao.save(role)`. On an **empty** `role` table this throws `IllegalArgumentException: Entity must not be null` and signup returns a blank page. There is no seeding mechanism in the repo (no `data.sql`/CommandLineRunner). Seed manually after the app has created the schema:
```bash
docker exec obank-mysql mysql -uroot -pavengers1993 -e \
  "INSERT INTO onlinebanking.role (role_id, name) VALUES (1,'ROLE_USER'),(2,'ROLE_ADMIN');"
```
This is a pre-existing app dependency, not a migration bug. A `data.sql` seed would be a good permanent fix.

## Build & run
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
cd UserFront
mvn -q clean package -DskipTests
java -jar target/*.jar > app.log 2>&1 &
# wait for "Started ... in N seconds", then check:
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/index   # expect 200
```

## Golden-path test cases
- **T1 Login page**: GET `/index` → renders "Please sign in" with username/password + Sign up link.
- **T2 Signup**: POST `/signup` (firstName,lastName,phone,email,username,password) → 302 redirect to `/`; verify a row in `onlinebanking.user` with populated `primary_account_id`/`savings_account_id`.
- **T3 Login + dashboard**: POST `/index` (username,password) → 302 to `/userFront`; page shows Primary Balance and Savings Balance (0.00 for new users).
- **T4 Wrong password**: POST `/index` with bad password → 302 to `/index?error`, banner "Invalid username and secret.", no dashboard access.

Quick curl smoke test for T2/T3 (cookie jar handles the session):
```bash
curl -s -c cj.txt -d "firstName=T&lastName=U&phone=555-555-1234&email=u1@example.com&username=u1&password=Password123" http://localhost:8080/signup -o /dev/null -w "signup %{http_code}\n"
curl -s -c cj.txt -b cj.txt -d "username=u1&password=Password123" http://localhost:8080/index -o /dev/null -w "login %{redirect_url}\n"
```

## Known failure modes & workarounds
- **Blank page on login + `StackOverflowError` in app.log**: Spring Security 6 `AuthenticationManager` built via `AuthenticationConfiguration.getAuthenticationManager()` can self-reference as its own parent → infinite recursion. Build it explicitly instead: `new ProviderManager(daoAuthenticationProvider)` with `setUserDetailsService` + `setPasswordEncoder`.
- **Blank page on signup + `Entity must not be null`**: role table not seeded (see Prerequisites).
- **`PatternParseException` on any URL**: Spring Security 6 `PathPatternParser` requires `**` to be terminal — use `/error/**`, not `/error/**/*`.
- **App fails to start with circular reference error**: `UserServiceImpl` ↔ `AccountServiceImpl` reference each other; `spring.main.allow-circular-references=true` is set in application.properties to preserve original wiring.
- **Transient `NonUniqueResultException` on a browser signup**: observed once, not reproducible via curl and no duplicate DB rows — likely a stale-session/double-submit artifact. If it recurs, clear browser cookies / use a fresh user.

## Devin Secrets Needed
None. MySQL runs locally in Docker with a local-only dev password (`avengers1993`); no external credentials are required.
