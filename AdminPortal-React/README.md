# AdminPortal-React

React (Vite + TypeScript) rewrite of the Angular `AdminPortal/` admin app for the
online banking system. This is a **parallel migration** — the original Angular
source under `AdminPortal/` is left untouched, and each page/feature is migrated
in its own PR.

## Stack

- [Vite](https://vite.dev/) + React 19 + TypeScript
- [React Router v7](https://reactrouter.com/) for routing
- Bootstrap 3 CSS + glyphicon fonts (copied from the Angular app to `public/assets/`)
- [Vitest](https://vitest.dev/) + React Testing Library for tests
- `oxlint` for linting

## Migrated in this PR

- Route `/savingsTransaction/:username` → `src/pages/SavingsTransaction.tsx`
  (ports `AdminPortal/src/app/savings-transaction/*`)
- `getSavingsTransactionList(username)` typed fetch module →
  `src/services/userService.ts` (ports the relevant part of
  `AdminPortal/src/app/user.service.ts`; `fetch` + `credentials: 'include'`
  replacing RxJS `Http` + `withCredentials`)
- `Transaction` interface → `src/types/transaction.ts`
- Shared shell: `Navbar` + router (`src/components/Navbar.tsx`, `src/App.tsx`)

The app talks to the Spring Boot `UserFront/` backend at `http://localhost:8080`
using session cookies (`credentials: 'include'`).

## Commands

```bash
npm install
npm run dev      # dev server on http://localhost:5173
npm run build    # type-check + production build to dist/
npm run lint     # oxlint
npm test         # vitest run
```

> Requires Node 20+.
