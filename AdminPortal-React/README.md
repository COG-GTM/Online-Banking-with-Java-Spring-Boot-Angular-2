# AdminPortal (React)

React (Vite) port of the original Angular 4 `AdminPortal` admin dashboard for the
Online Banking system. It talks to the `UserFront` Spring Boot backend
(`http://localhost:8080`) via REST and requires **no backend changes**.

## Features

- Login / logout (session cookie based, `withCredentials`)
- User account listing with enable/disable actions
- Primary & savings account transaction views
- Appointment listing with confirm action

## Tech stack

- [React](https://react.dev/) 19 + [Vite](https://vite.dev/)
- [react-router-dom](https://reactrouter.com/) for routing
- [axios](https://axios-http.com/) for HTTP
- Bootstrap 3 CSS (bundled in `public/assets/css/`, linked from `index.html`) to
  preserve the original look. `bootstrap` is also listed as a dependency.

## Project structure

```
src/
  components/   Navbar.jsx          shared navbar
  context/      AuthContext.jsx     auth state via React Context
  pages/        LoginPage.jsx, UserAccountPage.jsx, PrimaryTransactionPage.jsx,
                SavingsTransactionPage.jsx, AppointmentPage.jsx
  services/     loginService.js, userService.js, appointmentService.js
  utils/        formatDate.js       date formatting helpers
  App.jsx       routes + providers
  main.jsx      entrypoint
```

## Routes

| Path                              | Page                       |
| --------------------------------- | -------------------------- |
| `/`                               | redirects to `/login`      |
| `/login`                          | `LoginPage`                |
| `/userAccount`                    | `UserAccountPage`          |
| `/primaryTransaction/:username`   | `PrimaryTransactionPage`   |
| `/savingsTransaction/:username`   | `SavingsTransactionPage`   |
| `/appointment`                    | `AppointmentPage`          |

## Prerequisites

The backend must be running. From the repo root:

```bash
cd ../UserFront
mvn spring-boot:run   # requires a MySQL 'onlinebanking' database, serves on :8080
```

## Development

```bash
npm install
npm run dev      # starts the Vite dev server (default http://localhost:5173)
```

The dev server proxies `/api`, `/index`, and `/logout` to `http://localhost:8080`
(see `vite.config.js`). The service modules use **relative** URLs by default, so
requests go through this proxy and avoid CORS entirely (the backend's CORS filter
only allows the old Angular origin `http://localhost:4200`).

To point the app at a backend on a different origin (e.g. a production build),
set `VITE_API_BASE_URL`:

```bash
VITE_API_BASE_URL=https://api.example.com npm run build
```

## Production build

```bash
npm run build    # outputs static assets to dist/
npm run preview  # serves the production build locally
```

## Lint

```bash
npm run lint
```
