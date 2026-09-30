# AdminPortal

Angular admin UI for the Online Banking backend (`../UserFront`). Built with Angular CLI 20.3
(standalone components, zoneless change detection, `HttpClient` with `withFetch()`).

## Requirements

- Node.js `^20.19.0`, `^22.12.0` or `>=24.0.0` — required by Angular 20. With nvm: `nvm install 22 && nvm use 22`.
- npm 10+ (bundled with Node 22).

The backend is expected at `http://localhost:8080` (Spring form login at `/index`, session cookie,
`/api/user/**`, `/api/appointment/**`, `/logout`). Its CORS filter allows `http://localhost:4200`.

## Commands

| Command | Description |
| --- | --- |
| `npm install` | Install dependencies. |
| `npm start` | Dev server on http://localhost:4200 (`ng serve`). |
| `npm run build` | Production build into `dist/admin-portal`. |
| `npm run lint` | ESLint via angular-eslint (`ng lint`). |
| `npm test -- --watch=false` | Unit tests with the CLI's Vitest runner (jsdom, headless). |
| `npx playwright install chromium` | One-time download of the browser used by the e2e tests. |
| `npm run e2e` (`npx playwright test`) | Playwright e2e tests. Starts `ng serve` on port 4200 automatically and mocks the backend with `page.route`, so no backend is needed. |

## Notes

- Bootstrap 3 CSS/JS and jQuery are served from `src/assets` via the `styles`/`scripts` arrays in `angular.json`.
- Login state is kept in `localStorage` (`PortalAdminHasLoggedIn`); authentication itself is the backend session cookie (`withCredentials: true` on every request).
