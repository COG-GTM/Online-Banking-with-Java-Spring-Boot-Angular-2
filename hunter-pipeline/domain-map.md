# Domain Map — Online Banking (Java Spring Boot + Angular 2)

## Architecture Overview

- **Backend:** Spring Boot 1.5.4 (Java 8), Spring Security (form-based session auth), Hibernate/JPA, MySQL
- **Frontend (User):** Thymeleaf server-rendered templates
- **Frontend (Admin):** Angular 2/4 SPA communicating with REST API on same backend
- **Auth model:** Session-based form login, CSRF disabled, CORS restricted to localhost:4200
- **Security config:** All URLs require authentication EXCEPT `/`, `/about/**`, `/contact/**`, `/error/**/*`, `/console/**`, `/signup`, static assets

## Domain Matrix

| Domain | Abbrev | Key files | Endpoints | Exposure | Sensitive data | Recommended vuln classes |
|--------|--------|-----------|-----------|----------|----------------|-------------------------|
| Authentication & User Registration | AUTH | SecurityConfig.java, HomeController.java, UserSecurityService.java, UserServiceImpl.java | POST /signup, POST /index (login), GET /logout | external-unauth | Passwords, PII (email, phone) | AUTHZ, CRYPTOFAIL, MISCONFIG, SDE |
| Account Management | ACCT | AccountController.java, AccountServiceImpl.java, PrimaryAccount.java, SavingsAccount.java | GET/POST /account/primaryAccount, GET/POST /account/savingsAccount, GET/POST /account/deposit, GET/POST /account/withdraw | external-auth | Account balances, transaction history | AUTHZ, IDOR, INJ |
| Fund Transfer | XFER | TransferController.java, TransactionServiceImpl.java, Recipient.java, RecipientDao.java | GET/POST /transfer/betweenAccounts, GET/POST /transfer/recipient, GET /transfer/recipient/edit, GET /transfer/recipient/delete, GET/POST /transfer/toSomeoneElse | external-auth | Account numbers, transfer amounts, recipient PII | AUTHZ, IDOR, INJ, CSRF |
| Appointments | APPT | AppointmentController.java, AppointmentServiceImpl.java, Appointment.java | GET/POST /appointment/create | external-auth | User-banker meeting data | IDOR, INJ |
| Admin API | ADMIN | UserResource.java, AppointmentResource.java | GET /api/user/all, GET /api/user/primary/transaction, GET /api/user/savings/transaction, GET /api/user/{username}/enable, GET /api/user/{username}/disable, GET /api/appointment/all, GET /api/appointment/{id}/confirm | external-auth (ADMIN role) | All user data, all transactions, account enable/disable | AUTHZ, IDOR, PRIVESC, SDE |
| Configuration & Security Infrastructure | CONFIG | SecurityConfig.java, RequestFilter.java, application.properties | N/A (framework config) | N/A | DB credentials (hardcoded), BCrypt salt (hardcoded) | MISCONFIG, CRYPTOFAIL, SDE |
| Dependencies | DEPS | pom.xml, AdminPortal/package.json | N/A | N/A | N/A | DEPS |

## Cross-Domain Interactions

- **AUTH → ACCT:** User creation automatically provisions Primary + Savings accounts
- **XFER → ACCT:** Transfers modify account balances directly via shared DAOs
- **ADMIN → AUTH/ACCT/XFER:** Admin API exposes all user data and can enable/disable users
- **CONFIG → ALL:** Hardcoded DB password, static BCrypt salt, CSRF disabled, /console/** publicly accessible

## Key Security Observations (pre-scan)

1. **CSRF disabled globally** — all state-changing operations via POST are vulnerable to CSRF
2. **Hardcoded DB credentials** in application.properties (root/avengers1993)
3. **Static BCrypt salt** ("salt") — reduces password hashing security
4. **/console/** is permitAll — likely an H2/DB console exposed publicly
5. **No IDOR protection** — RecipientDao.findByName is a global lookup (not scoped to user)
6. **No input validation** — deposit/withdraw accept arbitrary amount strings parsed to double
7. **Admin API uses @PreAuthorize("hasRole('ADMIN')")** — but no client-side route guards in Angular
8. **Profile update has no ownership check** — POST /user/profile accepts username from form body

## M = 6 application domains + 1 DEPS domain = 7 total
## Planned scans = AUTH(4) + ACCT(3) + XFER(4) + APPT(2) + ADMIN(4) + CONFIG(3) + DEPS(1) = 21 sessions
