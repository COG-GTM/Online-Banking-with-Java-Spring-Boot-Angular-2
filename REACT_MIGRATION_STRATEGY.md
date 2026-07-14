# React + TypeScript Migration Strategy

Migration plan for moving the legacy front ends of **Online Banking with Java, Spring
Boot, Angular 2** to **React + TypeScript**, using an incremental *strangler-fig*
approach so the app keeps working at every step.

> **Status: planning only.** No application code is migrated by this document. Each
> phase below is scoped so a single follow-up Devin session can implement and verify it.

---

## 1. What we are migrating

The repo contains two independent legacy front ends that both talk to one Spring Boot
backend + MySQL database:

| App | Path | Stack | Port | Audience |
|-----|------|-------|------|----------|
| **UserFront** | `UserFront/` | Spring Boot 1.5.4 (Java 8), **Thymeleaf** server-rendered MVC, Spring Security form login, Hibernate/JPA, MySQL | `:8080` | Customers (`ROLE_USER`) |
| **AdminPortal** | `AdminPortal/` | **Angular 4** SPA (Angular CLI 1.1.2), RxJS 5, Bootstrap | `:4200` | Admins (`ROLE_ADMIN`) |

Key architectural fact that drives the whole plan:

- **The AdminPortal already consumes a JSON REST API** (`@RestController` classes under
  `/api/**`, secured with `@PreAuthorize("hasRole('ADMIN')")`). React can reuse this
  contract directly.
- **The UserFront customer flows have NO JSON API.** Every customer action
  (login, signup, deposit, withdraw, transfer, recipients, appointments, profile) is a
  server-rendered Thymeleaf page that submits an HTML **form POST** and receives a
  `redirect:`. There are no JSON endpoints for these flows today.

> **Therefore the migration is not a pure front-end swap.** Phase 0 must add a
> JSON/REST API layer to the backend (alongside the existing Thymeleaf controllers,
> which stay running) so the React customer portal has something typed to call. This is
> the single largest risk/effort item and is called out explicitly below.

---

## 2. How the legacy app was run locally (verified in the VM)

Both front ends were started and exercised end-to-end on the VM.

### 2.1 Database (MySQL 5.7 via Docker)

The legacy `mysql-connector-java` (5.1.x, pulled in by Spring Boot 1.5.4) is **not
compatible with MySQL 8** — it fails on the removed `query_cache_size` system variable
and on modern TLS defaults. A **MySQL 5.7** container is the frictionless choice:

```bash
docker run -d --name onlinebanking-mysql \
  -e MYSQL_ROOT_PASSWORD=avengers1993 \
  -e MYSQL_DATABASE=onlinebanking \
  -p 3306:3306 mysql:5.7
```

Credentials/DB name match `UserFront/src/main/resources/application.properties`
(`root` / `avengers1993`, schema `onlinebanking`). Hibernate `ddl-auto=update`
auto-creates the schema on first boot.

The `role` table is **not** auto-seeded but signup requires it (`Role.roleId` has no
`@GeneratedValue`), so seed the two roles once:

```sql
INSERT INTO role (role_id, name) VALUES (1,'ROLE_USER'),(2,'ROLE_ADMIN');
```

To create an admin, sign a user up through the UI, then point their `user_role` row at
`role_id = 2`.

### 2.2 UserFront (Spring Boot / Thymeleaf) — `:8080`

Runs on **Java 8** (the pom targets Java 1.8 / Spring Boot 1.5.4):

```bash
cd UserFront
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
mvn spring-boot:run           # serves http://localhost:8080/index
```

> If MySQL 8 must be used instead of 5.7, append
> `?useSSL=false&allowPublicKeyRetrieval=true` to `spring.datasource.url` — but the
> `query_cache_size` failure remains, so 5.7 is recommended for local runs.

### 2.3 AdminPortal (Angular 4) — `:4200`

Angular CLI 1.1.2 needs an old Node runtime; **Node 12** works (the project has no SCSS
so `node-sass` is not on the critical path):

```bash
cd AdminPortal
nvm use 12
npm install
node_modules/.bin/ng serve --host 0.0.0.0 --port 4200   # http://localhost:4200
```

### 2.4 End-to-end verification performed

- **UserFront**: signed up `john`, logged in, deposited `$500` to Primary and `$200` to
  Savings, transferred `$100` Primary→Savings. Dashboard then correctly showed
  **Primary $400 / Savings $300**, and the DB ledger recorded the transactions.
- **AdminPortal**: logged in as `admin`, the User Account page rendered a cross-origin
  (CORS, `withCredentials`) call to `GET /api/user/all` and showed `john`'s
  `400 / 300` balances — proving the Angular SPA ⇄ Spring backend integration works.

---

## 3. Legacy inventory

### 3.1 UserFront — MVC controllers & Thymeleaf routes (customer)

| Controller | HTTP route(s) | Template rendered | Behavior |
|-----------|---------------|-------------------|----------|
| `HomeController` | `GET /` → redirect `/index` | – | root redirect |
| `HomeController` | `GET /index` | `index.html` | login page (Spring Security form login target) |
| `HomeController` | `GET/POST /signup` | `signup.html` | create user (+ primary & savings accounts, `ROLE_USER`) |
| `HomeController` | `GET /userFront` | `userFront.html` | dashboard: primary + savings balances, quick links |
| `AccountController` | `GET /account/primaryAccount` | `primaryAccount.html` | primary balance + `PrimaryTransaction` list |
| `AccountController` | `GET /account/savingsAccount` | `savingsAccount.html` | savings balance + `SavingsTransaction` list |
| `AccountController` | `GET/POST /account/deposit` | `deposit.html` | deposit to Primary/Savings |
| `AccountController` | `GET/POST /account/withdraw` | `withdraw.html` | withdraw from Primary/Savings |
| `TransferController` | `GET/POST /transfer/betweenAccounts` | `betweenAccounts.html` | move funds Primary↔Savings |
| `TransferController` | `GET /transfer/recipient` | `recipient.html` | list recipients + add form |
| `TransferController` | `POST /transfer/recipient/save` | → redirect | create recipient |
| `TransferController` | `GET /transfer/recipient/edit?recipientName=` | `recipient.html` | edit recipient |
| `TransferController` | `GET /transfer/recipient/delete?recipientName=` | `recipient.html` | delete recipient |
| `TransferController` | `GET/POST /transfer/toSomeoneElse` | `toSomeoneElse.html` | transfer to a saved recipient |
| `UserController` | `GET/POST /user/profile` | `profile.html` | view/update profile |
| `AppointmentController` | `GET/POST /appointment/create` | `appointment.html` | schedule an appointment |
| Spring Security | `POST /index` (username, password, remember-me) | – | form login → `/userFront` |
| Spring Security | `GET /logout` | – | logout → `/index?logout` |

### 3.2 UserFront — REST API (`@RestController`, consumed by AdminPortal today)

Secured with `@PreAuthorize("hasRole('ADMIN')")`; session-cookie auth; CORS for
`http://localhost:4200` via `RequestFilter`.

| Resource | Endpoint | Returns |
|----------|----------|---------|
| `UserResource` | `GET /api/user/all` | `List<User>` (includes accounts, balances, authorities) |
| `UserResource` | `GET /api/user/primary/transaction?username=` | `List<PrimaryTransaction>` |
| `UserResource` | `GET /api/user/savings/transaction?username=` | `List<SavingsTransaction>` |
| `UserResource` | `GET /api/user/{username}/enable` | `void` (enable user) |
| `UserResource` | `GET /api/user/{username}/disable` | `void` (disable user) |
| `AppointmentResource` | `GET /api/appointment/all` | `List<Appointment>` |
| `AppointmentResource` | `GET /api/appointment/{id}/confirm` | `void` (confirm appointment) |

### 3.3 AdminPortal — Angular components, services & routes

| Route | Component | Service(s) | Backend call(s) |
|-------|-----------|-----------|-----------------|
| `''` → `/login` | – (redirect) | – | – |
| `/login` | `LoginComponent` | `LoginService` | `POST /index` (form login), `GET /logout` |
| `/userAccount` | `UserAccountComponent` | `UserService` | `GET /api/user/all`, enable/disable |
| `/primaryTransaction/:username` | `PrimaryTransactionComponent` | `UserService` | `GET /api/user/primary/transaction` |
| `/savingsTransaction/:username` | `SavingsTransactionComponent` | `UserService` | `GET /api/user/savings/transaction` |
| `/appointment` | `AppointmentComponent` | `AppointmentService` | `GET /api/appointment/all`, confirm |
| (shell) | `NavbarComponent` | `LoginService` | logout; `localStorage['PortalAdminHasLoggedIn']` gate |
| (shell) | `AppComponent` | – | router outlet |

### 3.4 Domain model (JPA entities → TypeScript types)

`User`, `PrimaryAccount`, `SavingsAccount`, `PrimaryTransaction`, `SavingsTransaction`,
`Recipient`, `Appointment`, and security entities `Role`, `UserRole`, `Authority`.

Shape notes for the typed client:
- `User` implements `UserDetails`; JSON includes `primaryAccount`, `savingsAccount`,
  `recipientList`, `authorities`. `password` is currently serialized (a bug to *not*
  reproduce — the React DTO must omit it).
- `PrimaryTransaction` / `SavingsTransaction`: `{ id, date (epoch ms), description,
  type, status, amount (double), availableBalance (BigDecimal) }`.
- `Account`: `{ id, accountNumber (int), accountBalance (BigDecimal) }`.
- `Appointment`: `{ id, date, location, description, confirmed }`.
- `Recipient`: `{ id, name, email, phone, accountNumber, description }`.

---

## 4. Target architecture

A **single** React + TypeScript SPA (Vite) replaces **both** legacy front ends, with two
role-guarded route trees sharing one API/auth layer. One app is simpler to build, test,
and deploy than two, and the two audiences already share the same backend and session
model.

| Concern | Choice | Rationale |
|---------|--------|-----------|
| Build tool | **Vite + React 18 + TypeScript** (strict) | Fast dev server, first-class TS, simple prod build |
| Routing | **React Router v6** | Nested routes + `loader`/guards for `/app` (user) vs `/admin` (admin) |
| Server state | **TanStack Query (React Query)** | Balances, transactions, users, appointments are server state — caching + refetch replace the manual `subscribe`/`location.reload()` pattern |
| Client/UI state | **React Context** (auth/session only) | Minimal; no Redux needed for this app's size |
| API client | **Typed `fetch` wrapper** + hand-written DTO types (or generated from an OpenAPI spec if one is added) | One place for `credentials: 'include'`, base URL, error mapping |
| Forms | **React Hook Form + Zod** | Deposit/withdraw/transfer/signup/profile forms with typed validation |
| Auth | Reuse **Spring Security session cookie** (`credentials: 'include'`), same model the Angular admin uses today | Lowest-risk; avoids a token/JWT rewrite in phase 0 |
| Styling | Reuse **Bootstrap 3 CSS** initially for visual parity | Keeps look-and-feel; can modernize later without blocking migration |
| Dev proxy | Vite proxies `/api`, `/index`, `/logout`, `/signup` → `http://localhost:8080` | Same-origin in dev; sidesteps CORS/CSRF friction |

### 4.1 Proposed app structure

```
webapp/                      # new Vite React app at repo root (name TBD)
  src/
    main.tsx
    app/router.tsx           # route tree: /login, /app/**, /admin/**
    lib/apiClient.ts         # typed fetch wrapper (credentials: 'include')
    lib/queryClient.ts       # TanStack Query config
    auth/AuthProvider.tsx    # session context + role guards
    types/                   # User, Account, Transaction, Appointment, Recipient
    features/
      auth/                  # login, logout
      dashboard/             # userFront
      accounts/              # primary/savings statements
      money/                 # deposit, withdraw, transfers, recipients
      appointments/          # create appointment
      profile/               # profile view/edit
      signup/                # public signup
      admin/                 # user list, txn views, appointments
    components/              # Navbar, layout, shared UI
```

### 4.2 Required backend work (strangler enabler, Phase 0)

Add JSON endpoints **alongside** the existing Thymeleaf controllers (do not delete
anything yet). Suggested additive contract for the customer portal:

```
POST /api/auth/login            # {username, password} -> session cookie + current user
POST /api/auth/logout
GET  /api/auth/me               # current user + role (drives guards)
POST /api/signup                # {firstName,lastName,username,email,password,phone}

GET  /api/account/primary       # balance + transactions for current principal
GET  /api/account/savings
POST /api/account/deposit       # {accountType, amount}
POST /api/account/withdraw      # {accountType, amount}

POST /api/transfer/betweenAccounts   # {transferFrom, transferTo, amount}
GET  /api/recipient                  # list for current principal
POST /api/recipient                  # create/update
DELETE /api/recipient/{name}
POST /api/transfer/toSomeoneElse     # {recipientName, accountType, amount}

GET/POST /api/user/profile
POST /api/appointment                 # {date, location, description}
```

Cross-cutting backend items (each an explicit Phase-0 success criterion):
- Configure **CORS** for the React dev/prod origin (generalize the current hardcoded
  `RequestFilter` for `:4200`).
- Decide **CSRF**: it is currently `disabled` globally. Keep disabled for cookie+JSON in
  the interim, or add a proper CSRF token flow (preferred long-term). Document the call.
- Return DTOs that **omit `password`** and avoid lazy-loading serialization traps.

---

## 5. Phased strangler-fig plan

Each phase is independently shippable, keeps the legacy apps running, and is sized for a
single Devin session. Order: shared foundation first, then features from **simplest →
most complex**. "Legacy behavior" success criteria are stated as concrete flows.

Legend for flows: **A/B/C** = the specific user journeys to match against the legacy app.

### Phase 0 — Scaffold + shared API/auth layer + backend JSON API
**Goal:** foundation everything else builds on.
- Scaffold Vite React TS app; add React Router, TanStack Query, RHF+Zod, Bootstrap CSS.
- Build `apiClient` (`credentials: 'include'`, base URL, error mapping) and DTO types
  from §3.4.
- Add backend JSON endpoints from §4.2 (auth/me + read endpoints at minimum) beside the
  Thymeleaf controllers; configure CORS; document CSRF decision.
- `AuthProvider` + role guards; Vite dev proxy to `:8080`.

**Success criteria**
- A) `POST /api/auth/login` with valid creds sets a session cookie; `GET /api/auth/me`
  returns the user + role.
- B) `GET /api/account/primary` (as `ROLE_USER`) and `GET /api/user/all` (as
  `ROLE_ADMIN`) return the same data the legacy dashboard/admin show for a seeded user.
- C) `npm run build` and `npm run lint` pass; unauthenticated access to a guarded route
  redirects to `/login`.

### Phase 1 — Auth shell (login / logout / guards / navbar)
**Goal:** replace the login pages + app shell for both audiences.
- React `/login`, logout, role-based redirect (`ROLE_USER` → `/app`, `ROLE_ADMIN` →
  `/admin`), shared Navbar mirroring the legacy menus.

**Success criteria — matches legacy for:**
- A) valid user login → lands on customer dashboard; valid admin login → lands on admin.
- B) wrong credentials → error shown, no navigation (parity with `/index?error`).
- C) logout clears the session and returns to `/login` (parity with `/logout`).

### Phase 2 — Customer dashboard (`/app` ← `userFront.html`)
**Goal:** read-only balances + quick links.
- **Success criteria:** A) shows Primary & Savings balances identical to
  `GET /userFront`; B) quick links route to statements/deposit/withdraw; C) refetches
  after a balance-changing action elsewhere (via React Query invalidation).

### Phase 3 — Account statements (`primaryAccount.html`, `savingsAccount.html`)
**Goal:** read-only transaction tables for both accounts.
- **Success criteria:** A) primary statement rows (date, description, type, status,
  amount, available balance) match the legacy table; B) savings statement matches;
  C) empty-state renders when there are no transactions.

### Phase 4 — Deposit & Withdraw (`deposit.html`, `withdraw.html`)
**Goal:** first *write* flows (simplest forms).
- **Success criteria:** A) deposit `$X` to Primary/Savings updates balance by `+X` and
  adds a "Deposit …" ledger row, matching legacy math; B) withdraw does `−X` with a
  "Withdraw …" row; C) invalid/empty amount is rejected client-side.

### Phase 5 — Transfer between accounts (`betweenAccounts.html`)
**Goal:** two-account atomic move.
- **Success criteria:** A) Primary→Savings of `$X` decrements Primary, increments
  Savings, records the transfer row (matches legacy); B) Savings→Primary works;
  C) same-account or invalid direction is rejected (legacy throws "Invalid Transfer").

### Phase 6 — Recipients + Transfer to someone else (`recipient.html`, `toSomeoneElse.html`)
**Goal:** recipient CRUD + external transfer (most complex customer flow).
- **Success criteria:** A) add/edit/delete recipient persists per-user; B) transfer to a
  saved recipient from Primary/Savings decrements the source and records the row;
  C) recipient list is scoped to the current user only.

### Phase 7 — Appointments (`appointment.html`)
**Goal:** schedule an appointment (date parsing).
- **Success criteria:** A) creating an appointment (date, location, description) persists
  it tied to the user; B) date is parsed/stored equivalently to the legacy
  `yyyy-MM-dd hh:mm`; C) it appears later in the admin appointment list (Phase 12).

### Phase 8 — Profile (`profile.html`)
**Goal:** view/edit profile.
- **Success criteria:** A) profile shows current first/last name, email, phone;
  B) saving updates persist; C) validation errors are surfaced.

### Phase 9 — Signup (`signup.html`, public)
**Goal:** public registration.
- **Success criteria:** A) new signup creates the user + primary & savings accounts +
  `ROLE_USER` (parity with `HomeController.signupPost`); B) duplicate username/email is
  rejected with the legacy messages; C) success routes to login.

### Phase 10 — Admin: user accounts (`/admin` ← `UserAccountComponent`)
**Goal:** first admin feature (reuses existing `/api/user/**`).
- **Success criteria:** A) table lists all users with balances + enabled state matching
  `GET /api/user/all`; B) enable/disable toggles persist and refresh; C) links navigate
  to per-user transaction views.

### Phase 11 — Admin: primary/savings transactions (`PrimaryTransactionComponent`, `SavingsTransactionComponent`)
**Goal:** per-user statement views for admins.
- **Success criteria:** A) `/admin/users/:username/primary` matches
  `GET /api/user/primary/transaction`; B) savings equivalent matches; C) unknown user
  handled gracefully.

### Phase 12 — Admin: appointments (`AppointmentComponent`)
**Goal:** list + confirm appointments.
- **Success criteria:** A) list matches `GET /api/appointment/all`; B) confirm calls
  `/api/appointment/{id}/confirm` and reflects the confirmed state; C) an appointment
  created in Phase 7 appears here.

### Phase 13 — Cutover & retirement
**Goal:** make React the front door; retire Thymeleaf + Angular.
- Serve the React build (static) from Spring Boot or a CDN; route legacy paths to the
  SPA; delete `AdminPortal/` and the Thymeleaf templates once parity is signed off; keep
  the JSON API as the single contract.
- **Success criteria:** A) all flows A/B/C from Phases 1–12 pass against the React app
  only; B) `UserFront/src/main/resources/templates/` and `AdminPortal/` removed with no
  broken references; C) a smoke run of signup → deposit → transfer → admin
  enable/disable → appointment confirm passes end-to-end.

---

## 6. Route / component inventory → React mapping

### 6.1 Customer (UserFront Thymeleaf → React)

| Legacy template | Legacy route | React route | React component(s) | Phase |
|-----------------|--------------|-------------|--------------------|-------|
| `index.html` | `GET /index`, `POST /index` | `/login` | `LoginPage` | 1 |
| `signup.html` | `GET/POST /signup` | `/signup` | `SignupPage` | 9 |
| `userFront.html` | `GET /userFront` | `/app` | `DashboardPage` | 2 |
| `primaryAccount.html` | `GET /account/primaryAccount` | `/app/accounts/primary` | `PrimaryStatementPage` | 3 |
| `savingsAccount.html` | `GET /account/savingsAccount` | `/app/accounts/savings` | `SavingsStatementPage` | 3 |
| `deposit.html` | `GET/POST /account/deposit` | `/app/deposit` | `DepositForm` | 4 |
| `withdraw.html` | `GET/POST /account/withdraw` | `/app/withdraw` | `WithdrawForm` | 4 |
| `betweenAccounts.html` | `GET/POST /transfer/betweenAccounts` | `/app/transfer/between` | `BetweenAccountsForm` | 5 |
| `recipient.html` | `/transfer/recipient[/save\|/edit\|/delete]` | `/app/recipients` | `RecipientsPage`, `RecipientForm` | 6 |
| `toSomeoneElse.html` | `GET/POST /transfer/toSomeoneElse` | `/app/transfer/external` | `ToSomeoneElseForm` | 6 |
| `appointment.html` | `GET/POST /appointment/create` | `/app/appointments/new` | `AppointmentForm` | 7 |
| `profile.html` | `GET/POST /user/profile` | `/app/profile` | `ProfilePage` | 8 |
| `common/header.html` | (fragment) | (layout) | `AppNavbar`, `AppLayout` | 1 |

### 6.2 Admin (Angular → React)

| Angular component | Angular route | React route | React component(s) | Phase |
|-------------------|---------------|-------------|--------------------|-------|
| `LoginComponent` | `/login` | `/login` (shared) | `LoginPage` | 1 |
| `NavbarComponent` | (shell) | (layout) | `AdminNavbar`, `AdminLayout` | 1 |
| `UserAccountComponent` | `/userAccount` | `/admin/users` | `UserAccountsPage` | 10 |
| `PrimaryTransactionComponent` | `/primaryTransaction/:username` | `/admin/users/:username/primary` | `AdminPrimaryTxnPage` | 11 |
| `SavingsTransactionComponent` | `/savingsTransaction/:username` | `/admin/users/:username/savings` | `AdminSavingsTxnPage` | 11 |
| `AppointmentComponent` | `/appointment` | `/admin/appointments` | `AdminAppointmentsPage` | 12 |

### 6.3 Angular services → React data layer

| Angular service | Methods | React equivalent |
|-----------------|---------|------------------|
| `LoginService` | `sendCredential`, `logout` | `auth` module + `apiClient` (`/api/auth/*`) |
| `UserService` | `getUsers`, `getPrimary/SavingsTransactionList`, `enable/disableUser` | `useUsers`, `useUserTransactions`, `useToggleUser` (TanStack Query hooks) |
| `AppointmentService` | `getAppointmentList`, `confirmAppointment` | `useAppointments`, `useConfirmAppointment` |

---

## 7. Risks & notes

- **Missing customer JSON API** is the critical path (Phase 0 backend work). Front-end
  phases 2–9 depend on it.
- **Legacy security posture is weak** (CSRF disabled, password serialized in `/api/user/all`,
  fixed BCrypt salt, hardcoded DB creds, `ddl-auto=update`). The migration should *not*
  faithfully reproduce these; fix DTO exposure and CSRF as part of Phase 0, and track the
  rest as follow-ups.
- **No automated tests** exist beyond `contextLoads()`. Add component/integration tests
  per phase so "matches legacy behavior" is enforced, not just asserted.
- **Old toolchains** (Java 8 / Spring Boot 1.5.4 / Node 12 / Angular CLI 1.1.2 / MySQL 5.7)
  are required to run the legacy app; a separate backend upgrade is out of scope here but
  worth sequencing after cutover.
