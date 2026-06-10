# Agents Configuration

## Project Context
Online Banking application with Java 25, Spring Boot 3.4.x, Spring Security 6.x, Hibernate 6.x, MySQL 8.x, Thymeleaf 3.x, Maven.

## Coding Standards

### Java
- Target: Java 25. Use modern Java features where beneficial (records for DTOs, pattern matching, text blocks, sealed classes).
- All new code must compile cleanly with zero warnings on JDK 25.
- No `javax.persistence` or `javax.servlet` imports — use `jakarta.*` exclusively.
- Use constructor injection over field injection (`@Autowired` on fields is discouraged).
- Use `Optional` return types for repository finder methods that may return null.
- No `System.out.println` — use SLF4J `LoggerFactory.getLogger()`.

### Spring Boot
- Version: 3.4.x (latest stable).
- Security: Use `SecurityFilterChain` bean configuration. NEVER use `WebSecurityConfigurerAdapter` (deleted in Spring Security 6).
- Use `@EnableMethodSecurity` (not `@EnableGlobalMethodSecurity`).
- Properties in `application.yml` or `application.properties` — no legacy datasource properties (`testWhileIdle`, `validationQuery`).
- Use HikariCP datasource properties natively.

### JPA / Hibernate
- Use `GenerationType.IDENTITY` for MySQL auto-increment columns.
- Do NOT hardcode Hibernate dialect — let Hibernate 6 auto-detect.
- Entity classes must use `jakarta.persistence.*` annotations.
- Use `findById()` returning `Optional` — never use the removed `findOne()`.

### Spring Security
- CORS: Configure via `WebMvcConfigurer` bean or Spring Security's `.cors()` DSL — not manual servlet filters.
- URL matching: Use `requestMatchers()` — never `antMatchers()` (removed in Security 6).
- Form login, logout, remember-me config via lambda DSL on `HttpSecurity`.

### Testing
- All controllers should have MockMvc integration tests.
- Service layer should have unit tests with mocked repositories.
- Use `@SpringBootTest` for integration tests.

### Code Quality
- No raw exception swallowing — log or rethrow.
- No hardcoded credentials in source — use environment variables or Spring profiles.
- Use SLF4J for all logging. No `System.out.println`.
