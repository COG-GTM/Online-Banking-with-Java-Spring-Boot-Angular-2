# AdminPortal-React

React + TypeScript (Vite) front end that is progressively replacing the two
legacy front ends of this repo (the Thymeleaf **UserFront** customer portal and
the Angular **AdminPortal**), following `../REACT_MIGRATION_STRATEGY.md`.

This is **Phase 0 + Phase 1**: the shared foundation (typed API client, auth
context, role guards, routing, Bootstrap UI) plus the auth shell (login, logout,
role-based redirect, navbar). Later phases migrate the remaining routes.

## Stack

- React 18 + TypeScript (strict) + Vite
- React Router v6 (role-guarded route trees: `/app` for `ROLE_USER`, `/admin` for `ROLE_ADMIN`)
- TanStack Query (server state), React Hook Form + Zod (forms)
- Bootstrap 5 CSS

## Prerequisites

The Spring Boot **UserFront** backend must be running on `:8080` (see the repo
root README / `REACT_MIGRATION_STRATEGY.md §2`). It exposes the JSON API this app
consumes (`/api/auth/*`, `/api/account/*`, `/api/user/*`).

## Develop

```bash
npm install
npm run dev      # http://localhost:5173
```

Vite proxies `/api`, `/index`, `/logout`, `/signup` to `http://localhost:8080`,
so the browser stays same-origin and the Spring Security session cookie flows
through unchanged.

## Scripts

```bash
npm run lint     # eslint
npm run build    # type-check + production build to dist/
npm run preview  # preview the production build
```
