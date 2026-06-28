# AdminPortal-React

React + TypeScript (Vite) rewrite of the Angular `AdminPortal/` admin app for the
Online Banking system. This is a **parallel migration**: the Angular source under
`AdminPortal/` is left untouched, and each page/feature is ported in its own PR.

## Migrated in this PR

- **`/userAccount`** — admin user-management table (User Name, First/Last Name,
  Email, Phone, Primary Account balance, Savings Account balance, Enabled,
  Action). The balance cells link to `/primaryTransaction/:username` and
  `/savingsTransaction/:username`. The Action column shows **Enable** (when the
  user is disabled) or **Disable** (when enabled); after the mutation the list is
  refreshed.

Foundation scaffolded so this page builds/renders standalone: Vite + React +
TypeScript project, React Router v6, the app shell + navbar, the typed user API
module, and Bootstrap 3 CSS + glyphicon fonts (copied to `public/assets/`).

Other routes (`/login`, `/primaryTransaction`, `/savingsTransaction`,
`/appointment`) are owned by other migration slices and are not wired here.

## Stack

- [Vite](https://vite.dev/) + React + TypeScript
- [React Router v6](https://reactrouter.com/)
- Plain `fetch` + async/await (RxJS `Http` replaced); `AbortController` used in
  `useEffect` cleanup to cancel in-flight requests on unmount
- Bootstrap 3 CSS (carried over from the Angular app to avoid styling drift)
- [Vitest](https://vitest.dev/) + React Testing Library

## Backend

API calls target the Spring Boot `UserFront/` backend at `http://localhost:8080`
and are session-cookie based (`credentials: 'include'`, equivalent to Angular's
`withCredentials: true`). The base URL can be overridden with the
`VITE_API_BASE_URL` environment variable.

Relevant endpoints:

- `GET /api/user/all`
- `GET /api/user/{username}/enable`
- `GET /api/user/{username}/disable`

## Scripts

```bash
npm install        # install dependencies
npm run dev        # dev server on http://localhost:5173
npm run build      # type-check + production build to dist/
npm run lint       # eslint
npm test           # run Vitest test suite
```

Requires Node 20+.

## Project structure

```
src/
├── components/   # app shell components (Navbar)
├── pages/        # route-level pages (UserAccount)
├── services/     # typed fetch modules (userService)
├── types/        # TypeScript interfaces (User)
├── styles/       # global CSS ported from Angular styles.css
├── test/         # Vitest setup
├── config.ts     # API base URL
├── App.tsx       # router + shell
└── main.tsx      # entrypoint
```
