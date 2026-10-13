# AdminPortal

Admin UI for the Online Banking app, built with [Angular](https://angular.dev) 22 (standalone components, signals, zoneless change detection). It talks to the `UserFront` Spring Boot backend on `http://localhost:8080` (configured in `src/environments/`), which only allows CORS from `http://localhost:4200`.

## Prerequisites

- Node.js `^22.22.3` or `^24.15.0` (required by Angular CLI 22)
- `npm install`

## Development server

Run `npm start` (`ng serve`) and open `http://localhost:4200/`. Log in with an admin account from the UserFront database.

## Build

Run `npm run build` (`ng build`). Artifacts go to `dist/admin-portal/browser`. The production configuration (default) swaps in `src/environments/environment.prod.ts`.

## Lint

Run `npm run lint` (`ng lint`, ESLint via angular-eslint).

## Unit tests

Run `npm test` (`ng test`, Vitest + jsdom). Add `-- --watch=false` for a single run.

## End-to-end tests

Run `npx playwright install chromium` once, then `npm run e2e`. The Playwright spec in `e2e/` starts `ng serve` and mocks the backend API, so UserFront does not need to be running.
