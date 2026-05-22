# Decoupling Plan — Online Banking Application

## Executive Summary

This document outlines a phased plan to decouple the monolithic Online Banking application into well-defined domain modules. The current architecture suffers from tight cross-domain coupling, a "god object" User entity, circular service dependencies, and controllers that directly access DAOs. This plan progressively resolves these issues while maintaining functionality at every step.

## Current Architecture

The application is a Spring Boot monolith with:
- **Domain entities** in `com.userFront.domain` — `User`, `PrimaryAccount`, `SavingsAccount`, `PrimaryTransaction`, `SavingsTransaction`, `Appointment`, `Recipient`
- **Security entities** in `com.userFront.domain.security` — `Role`, `UserRole`, `Authority`
- **DAOs** in `com.userFront.dao` — one per entity
- **Services** in `com.userFront.service` (interfaces) and `com.userFront.service.UserServiceImpl` (implementations)
- **Controllers** in `com.userFront.controller` — `HomeController`, `AccountController`, `AppointmentController`, `TransferController`, `UserController`
- **REST resources** in `com.userFront.resource` — `AppointmentResource`, `UserResource`
- **Config** in `com.userFront.config` — `SecurityConfig`, `RequestFilter`

## Cross-Domain Coupling Analysis

### 1. User God Object
`User.java` holds direct JPA relationships to every domain:
- `@OneToOne PrimaryAccount primaryAccount`
- `@OneToOne SavingsAccount savingsAccount`
- `@OneToMany List<Appointment> appointmentList`
- `@OneToMany List<Recipient> recipientList`
- `@OneToMany Set<UserRole> userRoles`
- Implements `UserDetails` (mixes identity with Spring Security)

### 2. Circular Service Dependencies
- `UserServiceImpl` → `AccountService` (creates accounts during user registration)
- `AccountServiceImpl` → `UserService` (looks up user for deposit/withdraw)
- `TransactionServiceImpl` → `UserService` (looks up user for transaction lists)

### 3. Controller Issues
- `HomeController` directly injects `RoleDao` instead of going through a service
- `AccountController` depends on three services: `UserService`, `AccountService`, `TransactionService`
- `AppointmentController` depends on `UserService` to resolve the user entity

### 4. Mixed Concerns in TransactionService
`TransactionService` handles both ledger operations (deposits, withdrawals, transfers) and recipient management (CRUD for `Recipient` entities).

## Dependency Graphs

### Before (Current State)
```
HomeController ──→ UserService ──→ AccountService ──→ UserService (CIRCULAR)
       │                                    │
       └──→ RoleDao (DIRECT DAO ACCESS)     └──→ TransactionService ──→ UserService
                                                         │
AccountController ──→ UserService                        └──→ RecipientDao
                 ──→ AccountService
                 ──→ TransactionService

AppointmentController ──→ UserService
                     ──→ AppointmentService

TransferController ──→ UserService
                  ──→ TransactionService
```

### After (Target State)
```
identity/HomeController ──→ UserRegistrationService ──→ UserService
                                                   ──→ AccountService

account/AccountController ──→ AccountService ──→ LedgerService (via interface)

appointment/AppointmentController ──→ AppointmentService (uses Principal directly)

transaction/TransferController ──→ LedgerService
                              ──→ RecipientService
                              ──→ UserLookup (interface)
```

## Phase 0 — Preparatory Refactoring

**Must complete before all other phases.**

### 0.1 — Break the User God Object (ID-based references)
- Replace `@OneToOne PrimaryAccount` with `Long primaryAccountId`
- Replace `@OneToOne SavingsAccount` with `Long savingsAccountId`
- Remove `@OneToMany List<Appointment> appointmentList` (query from Appointment side)
- Remove `@OneToMany List<Recipient> recipientList` (query from Recipient side)
- Keep `@OneToMany Set<UserRole> userRoles` (same domain)
- Update all dependent code to use ID-based lookups

### 0.2 — Decouple User from UserDetails
- Remove `implements UserDetails` from `User`
- Create `UserDetailsAdapter` in `com.userFront.security` that wraps `User`
- Update `UserSecurityService` to return `UserDetailsAdapter`

### 0.3 — Break Circular User ↔ Account Service Dependency
- Remove `AccountService` injection from `UserServiceImpl`
- Remove `UserService` injection from `AccountServiceImpl`
- Refactor `AccountServiceImpl.deposit()`/`withdraw()` to accept account entities directly
- Create `UserRegistrationService` orchestrator for signup flow
- Update `HomeController` to use `UserRegistrationService`

### 0.4 — Fix HomeController Direct DAO Access
- Remove `RoleDao` injection from `HomeController`
- Add `getDefaultUserRole()` to `UserService`

## Phase 1 — Extract Appointment Module

**Can run in parallel with Phases 2 and 3 after Phase 0 completes.**

- Create package `com.userFront.appointment` with sub-packages: `domain`, `dao`, `service`, `controller`, `resource`
- Move `Appointment.java` → replace `@ManyToOne User` with `Long userId`
- Move `AppointmentDao`, `AppointmentService`/impl, `AppointmentController`, `AppointmentResource`
- Controller resolves user via `Principal.getName()` directly

## Phase 2 — Extract Transaction & Transfer Module

**Can run in parallel with Phases 1 and 3 after Phase 0 completes.**

- Split `TransactionService` into `LedgerService` and `RecipientService`
- Create package `com.userFront.transaction` with sub-packages: `domain`, `dao`, `service`, `controller`
- Move `PrimaryTransaction`, `SavingsTransaction`, `Recipient` → replace object refs with Long IDs
- Move DAOs and `TransferController`
- Controller uses `UserLookup` interface for user resolution

## Phase 3 — Extract Account Module

**Can run in parallel with Phases 1 and 2 after Phase 0 completes.**

- Create package `com.userFront.account` with sub-packages: `domain`, `dao`, `service`, `controller`
- Move `PrimaryAccount`, `SavingsAccount` and their DAOs
- Move `AccountService`/impl and `AccountController`
- Refactor deposit/withdraw to call `LedgerService` via interface

## Phase 4 — Refine Identity & Security Module

**Must complete after Phases 1-3.**

- Create package `com.userFront.identity` with sub-packages: `domain`, `dao`, `service`, `controller`, `config`, `api`
- Move `User`, `Role`, `UserRole`, `Authority` → `identity/domain`
- Move DAOs, services, controllers, and security config
- Create `UserLookup` interface in `identity/api`
- Ensure all modules depend only on `UserLookup`, not `UserService` directly

## Phase 5 — Verification

- Ensure application compiles after each phase
- Update test classes for new package structures
- Verify no circular dependencies remain

## Execution Order

```
Phase 0 (sequential, must finish first)
    ├── 0.1 Break User god object
    ├── 0.2 Decouple User from UserDetails
    ├── 0.3 Break circular User↔Account dependency
    └── 0.4 Fix HomeController direct DAO access
         │
         ▼
┌────────┼────────┐
│        │        │
Phase 1  Phase 2  Phase 3   (parallel)
│        │        │
└────────┼────────┘
         │
         ▼
      Phase 4 (sequential, after 1-3)
         │
         ▼
      Phase 5: Test & verify
```
