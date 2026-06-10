# Skills & Conventions

## Architecture
- **Pattern**: MVC with service layer. Controllers → Services → DAOs (Repositories).
- **Two systems**: UserFront (Spring Boot + Thymeleaf) serves end users. AdminPortal (Angular, separate) consumes REST API from UserFront.
- **Security**: Role-based (ROLE_USER, ROLE_ADMIN). Users access Thymeleaf views. Admins access `/api/**` REST endpoints.

## Business Rules (MUST be preserved — zero deprecation)
1. User signup creates both a PrimaryAccount and SavingsAccount automatically with zero balance.
2. Passwords are hashed with BCrypt (strength 12, seeded SecureRandom with "salt").
3. Users can deposit/withdraw from Primary or Savings accounts independently.
4. Transfers between own accounts debit one and credit the other; a transaction record is created.
5. Transfers to others require a saved Recipient. The sender's account is debited. A transaction record is created.
6. Every deposit/withdraw/transfer creates a PrimaryTransaction or SavingsTransaction record.
7. Admins can enable/disable users via `/api/user/{username}/enable` and `/api/user/{username}/disable`.
8. Admins can view any user's transactions via `/api/user/primary/transaction?username=X` and `/api/user/savings/transaction?username=X`.
9. Admins can list all users via `/api/user/all`.
10. Admins can list all appointments via `/api/appointment/all` and confirm via `/api/appointment/{id}/confirm`.
11. Duplicate username or email is rejected at signup.
12. All `/account/**`, `/transfer/**`, `/user/**`, `/appointment/**` endpoints require authentication.
13. Public pages: `/`, `/index`, `/signup`, `/about/**`, `/contact/**`, `/webjars/**`, static resources.
14. Login page: `/index`. Successful login redirects to `/userFront`. Failed login redirects to `/index?error`.
15. Logout at `/logout` redirects to `/index?logout` and deletes `remember-me` cookie.
16. Remember-me is enabled.

## REST API Contracts (AdminPortal Angular depends on these — DO NOT CHANGE)
- GET `/api/user/all` → `List<User>`
- GET `/api/user/primary/transaction?username=X` → `List<PrimaryTransaction>`
- GET `/api/user/savings/transaction?username=X` → `List<SavingsTransaction>`
- GET/POST `/api/user/{username}/enable` → void
- GET/POST `/api/user/{username}/disable` → void
- GET `/api/appointment/all` → `List<Appointment>`
- GET `/api/appointment/{id}/confirm` → void
- CORS allowed origin: `http://localhost:4200`

## Entity List (all require javax→jakarta migration)
User, PrimaryAccount, SavingsAccount, PrimaryTransaction, SavingsTransaction, Appointment, Recipient, Role, UserRole

## File Naming
- Controllers: `*Controller.java` in `controller/` package
- REST APIs: `*Resource.java` in `resource/` package
- Services: `*Service.java` (interface) + `*ServiceImpl.java` (implementation) in `service/` and `service/UserServiceImpl/`
- Repositories: `*Dao.java` in `dao/` package
- Entities: `domain/` package, security entities in `domain/security/`
- Config: `config/` package
