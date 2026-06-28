# Admin Portal — React

React + TypeScript rewrite of the Angular `AdminPortal/` admin app, built with
[Vite](https://vite.dev/). It talks to the Spring Boot `UserFront/` backend on
`http://localhost:8080`. All API calls are session-cookie based and use
`credentials: "include"` (the Angular code used `withCredentials: true`).

> **Parallel migration in progress.** The Angular app is being ported one
> page/feature at a time, each in its own PR. This branch migrates only the
> **Primary Transaction** slice; sibling routes (`/login`, `/userAccount`,
> `/savingsTransaction/:username`, `/appointment`) are owned by other slices and
> are intentionally not wired into the router yet.

## Stack

- Vite + React 19 + TypeScript
- React Router v6+ for routing
- `fetch` + `async/await` (with `AbortController`) instead of RxJS `Http`
- Bootstrap 3 CSS + glyphicon fonts copied verbatim to `public/assets/`
- Vitest + React Testing Library for tests

## Scripts

```bash
npm install      # install dependencies (Node 20+)
npm run dev      # dev server on http://localhost:5173
npm run build    # type-check + production build to dist/
npm run lint     # eslint
npm test         # run the Vitest suite once
```

## Migrated route

| Route                          | Page component                  | API |
| ------------------------------ | ------------------------------- | --- |
| `/primaryTransaction/:username`| `src/pages/PrimaryTransaction`  | GET `/api/user/primary/transaction?username=...` |

## Layout

```
src/
├── types/        # TypeScript interfaces (Transaction)
├── services/     # typed fetch modules (userService, api base URL)
├── components/   # shared shell (Layout, Navbar)
├── pages/        # route-level page components (PrimaryTransaction)
├── utils/        # pipe → utility conversions (formatDate)
├── test/         # Vitest setup
├── App.tsx       # router
└── main.tsx      # entry point
```

## Angular → React mapping

| Angular                                           | React                                   |
| ------------------------------------------------- | --------------------------------------- |
| `UserService.getPrimaryTransactionList` (RxJS)    | `services/userService.ts` (`fetch`)     |
| `ActivatedRoute` params                           | `useParams`                             |
| constructor subscribe + `ngOnInit`                | `useEffect` + `AbortController` cleanup  |
| `*ngFor`                                           | `Array.map` with `key`                  |
| `date: 'MM/dd/yyyy'` pipe                          | `utils/date.ts` `formatDate`            |
| `app.component.html` shell / `navbar.component`    | `components/Layout` / `components/Navbar`|
