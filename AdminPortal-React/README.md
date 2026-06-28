# AdminPortal (React)

React + TypeScript + Vite migration of the Angular `AdminPortal/` admin app for
the Online Banking system. It talks to the Spring Boot `UserFront/` backend on
`http://localhost:8080` using session cookies (`credentials: 'include'`).

> Parallel migration: this PR migrates the **login / authentication flow and the
> shared app shell** only. The `userAccount`, `appointment`, and transaction
> routes are rendered as placeholders and are implemented in separate sessions.

## Stack

- Vite + React 19 + TypeScript
- React Router v7 (`/` redirects to `/login`)
- Plain `fetch` / async-await (no RxJS); `AbortController` for request cancellation
- Bootstrap 3 CSS + glyphicon fonts (copied to `public/assets/`)
- Vitest + React Testing Library

## Scripts

```bash
npm install      # install dependencies (Node 18+; developed on Node 22)
npm run dev      # dev server on http://localhost:5173
npm run build    # type-check + production build to dist/
npm run lint     # eslint
npm run test     # run the Vitest suite
```

## Configuration

The backend base URL defaults to `http://localhost:8080` and can be overridden
with the `VITE_API_BASE_URL` environment variable.

## Structure

```
src/
├── components/   # Navbar (app shell)
├── context/      # AuthProvider + auth context (session state, localStorage)
├── hooks/        # useAuth
├── pages/        # Login, Placeholder (stubs for other sessions' routes)
├── services/     # authService (typed fetch port of Angular LoginService), config
├── types/        # TypeScript interfaces
└── styles/       # global styles ported from Angular src/styles.css
```
