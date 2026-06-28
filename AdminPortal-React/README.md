# Admin Portal (React)

React + TypeScript rewrite of the Angular `AdminPortal/` admin app, built with Vite.
This is a parallel migration: the original Angular source under `AdminPortal/` is left
untouched while each page is ported into this project.

## Stack

- [Vite](https://vite.dev/) + React 19 + TypeScript
- React Router v7 (`react-router-dom`)
- Plain `fetch` + `async/await` (replacing Angular `Http`/RxJS); `AbortController`
  for request cancellation on unmount
- Bootstrap 3 CSS + glyphicon fonts (copied to `public/assets/`) to match the
  original styling
- Vitest + React Testing Library for tests

## Backend

The app talks to the Spring Boot `UserFront/` backend at `http://localhost:8080`.
All requests are session-cookie based and use `credentials: "include"`.

## Scripts

```bash
npm install        # install dependencies
npm run dev        # dev server on http://localhost:5173
npm run build      # type-check + production build to dist/
npm run lint       # eslint
npm test           # run the Vitest suite
```

## Migrated routes

| Route          | Page                | Source (Angular)                              |
| -------------- | ------------------- | --------------------------------------------- |
| `/appointment` | `AppointmentPage`   | `AdminPortal/src/app/appointment/*`           |

Other routes from the Angular app (`/login`, `/userAccount`, transactions) are being
migrated in parallel by separate efforts.

## Structure

```
src/
├── components/   # shared UI (Navbar, Layout shell)
├── pages/        # route-level pages (AppointmentPage)
├── services/     # typed fetch modules (appointmentService)
├── types/        # TypeScript interfaces (Appointment)
├── utils/        # helpers ported from Angular pipes (date formatting)
├── App.tsx       # router + shell wiring
└── main.tsx      # entry point
```
