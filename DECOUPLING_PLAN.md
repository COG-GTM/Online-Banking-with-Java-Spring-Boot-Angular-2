# Decoupling Plan: Online Banking Monolith

## 1. Executive Summary

This document outlines a phased plan to decompose the `UserFront` monolith into domain-aligned packages, reduce coupling between modules, and prepare the codebase for eventual extraction into independent microservices. The approach prioritises **zero behavioural change** in early phases so that every step can be validated by the existing test suite and manual smoke tests.

## 2. Current Architecture

All business logic lives under a flat package structure:

```
com.userFront/
├── config/          SecurityConfig, RequestFilter
├── controller/      AccountController, AppointmentController, HomeController,
│                    TransferController, UserController
├── dao/             AppointmentDao, PrimaryAccountDao, PrimaryTransactionDao,
│                    RecipientDao, RoleDao, SavingsAccountDao,
│                    SavingsTransactionDao, UserDao
├── domain/          Appointment, PrimaryAccount, PrimaryTransaction, Recipient,
│                    SavingsAccount, SavingsTransaction, User
│   └── security/    Authority, Role, UserRole
├── resource/        AppointmentResource, UserResource
└── service/         AccountService, AppointmentService, TransactionService,
                     UserService
    └── UserServiceImpl/  AccountServiceImpl, AppointmentServiceImpl,
                          TransactionServiceImpl, UserSecurityService,
                          UserServiceImpl
```

### Problems

| # | Problem | Impact |
|---|---------|--------|
| 1 | `User` is a God Object — owns accounts, appointments, recipients **and** implements `UserDetails` | Every domain depends on Identity |
| 2 | `UserServiceImpl` creates accounts directly via `AccountService` | Identity → Account coupling |
| 3 | `AccountServiceImpl` depends on `UserService` to resolve the principal | Account → Identity circular dependency |
| 4 | `TransactionServiceImpl` depends on `UserService` | Transaction → Identity coupling |
| 5 | `TransactionService` mixes transaction logic with recipient CRUD | Single Responsibility violation |
| 6 | `Appointment` holds a JPA `@ManyToOne User` reference | Appointment → Identity entity coupling |
| 7 | Flat packages make it impossible to enforce module boundaries | No compile-time isolation |

## 3. Domain Boundaries

Five bounded contexts emerge from the codebase:

| Domain | Responsibility | Key Entities |
|--------|---------------|-------------|
| **Identity** | Authentication, user profile, roles | User, Role, UserRole, Authority |
| **Account** | Primary & savings account lifecycle | PrimaryAccount, SavingsAccount |
| **Transaction** | Deposits, withdrawals, transfers, transaction history | PrimaryTransaction, SavingsTransaction |
| **Transfer** | Recipient management, inter-user transfers | Recipient |
| **Appointment** | Scheduling & confirming banker appointments | Appointment |

Cross-cutting: `config/` (security, CORS), `admin/resource/` (REST API for the Angular admin portal).

## 4. Coupling Hotspots

```
Identity ←──── Account        (AccountServiceImpl → UserService)
Identity ←──── Transaction    (TransactionServiceImpl → UserService)
Identity ←──── Appointment    (Appointment.user @ManyToOne)
Identity ←──── Transfer       (Recipient.user @ManyToOne)
Account  ←──── Transaction    (TransactionServiceImpl → PrimaryAccountDao, SavingsAccountDao)
Identity ────→ Account        (UserServiceImpl → AccountService.createPrimaryAccount/createSavingsAccount)
```

## 5. Dependency Graph (Target State)

```
                  ┌──────────┐
                  │ Identity │
                  └────┬─────┘
                       │ publishes UserCreatedEvent
         ┌─────────────┼─────────────┐
         ▼             ▼             ▼
    ┌─────────┐  ┌───────────┐  ┌────────────┐
    │ Account │  │ Appointmt │  │  Transfer  │
    └────┬────┘  └───────────┘  └────────────┘
         │
         ▼
   ┌─────────────┐
   │ Transaction │
   └─────────────┘
```

All arrows point **downward** — no cycles.

## 6. Phased Plan

### Phase 0 — Restructure into Domain Packages (this PR)

Move every source file into its domain-aligned sub-package. **No logic changes** — only file moves, package declarations, and import updates.

Target layout:

```
com.userFront/
├── identity/
│   ├── domain/           User.java
│   │   └── security/     Authority.java, Role.java, UserRole.java
│   ├── dao/              UserDao.java, RoleDao.java
│   ├── service/          UserService.java, UserServiceImpl.java, UserSecurityService.java
│   └── controller/       UserController.java, HomeController.java
├── account/
│   ├── domain/           PrimaryAccount.java, SavingsAccount.java
│   ├── dao/              PrimaryAccountDao.java, SavingsAccountDao.java
│   ├── service/          AccountService.java, AccountServiceImpl.java
│   └── controller/       AccountController.java
├── transaction/
│   ├── domain/           PrimaryTransaction.java, SavingsTransaction.java
│   ├── dao/              PrimaryTransactionDao.java, SavingsTransactionDao.java
│   └── service/          TransactionService.java, TransactionServiceImpl.java
├── transfer/
│   ├── domain/           Recipient.java
│   ├── dao/              RecipientDao.java
│   └── controller/       TransferController.java
├── appointment/
│   ├── domain/           Appointment.java
│   ├── dao/              AppointmentDao.java
│   ├── service/          AppointmentService.java, AppointmentServiceImpl.java
│   └── controller/       AppointmentController.java
├── admin/
│   └── resource/         UserResource.java, AppointmentResource.java
└── config/               SecurityConfig.java, RequestFilter.java
```

### Phase 1 — Decompose User God Object + UserCreatedEvent

- Remove account, appointment, and recipient associations from `User`
- Extract `UserPrincipal` adapter (implements `UserDetails`)
- Replace `AccountService` call in `UserServiceImpl` with a `UserCreatedEvent`
- Add `@EventListener` in `AccountServiceImpl` to create accounts on user creation
- Add `userId` field to `PrimaryAccount` and `SavingsAccount`

### Phase 2 — Extract RecipientService + Clean Appointment Module

- Create `RecipientService` / `RecipientServiceImpl` in the transfer package
- Move recipient CRUD out of `TransactionServiceImpl`
- Replace `@ManyToOne User` in `Appointment` with `Long userId`
- Update `AppointmentServiceImpl` and controller accordingly

### Phase 3 — Break Circular Service Dependencies

- Remove `UserService` dependency from `AccountServiceImpl` — controllers resolve the user
- Remove `UserService` dependency from `TransactionServiceImpl` — query by account ID
- Change service method signatures to accept IDs instead of `Principal` / usernames
- Update all callers (controllers, admin resources)

### Phase 4+ (Future)

- Extract each domain into a separate Spring Boot module (multi-module Maven build)
- Introduce API Gateway for the Angular admin portal
- Replace JPA cross-domain references with REST or messaging

## 7. Risk Mitigation

| Risk | Mitigation |
|------|-----------|
| Import errors after file moves | `mvn clean compile` gate after Phase 0 |
| Runtime wiring failures | `@SpringBootApplication` base package is `com.userFront` — all sub-packages auto-scanned |
| Angular admin portal breaks | REST endpoint paths (`/api/**`) are unchanged |
| Thymeleaf templates reference FQCNs | Unlikely — checked, templates use model attributes only |
| Database schema changes | Phase 0 has none; Phases 1-3 add columns via migration scripts |
