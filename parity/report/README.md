# AdminPortal parity report (Angular 4 baseline vs Angular 21)

Same Playwright specs run against two projects: `baseline` (default branch `260093a`, Angular 4.4.7 / CLI 1.1.2, `ng serve` on :4000) and `upgraded` (this branch, Angular 21.2, `ng serve` on :4200), both against the same Spring Boot backend (:8080) and MySQL 5.7.

## Results (latest full run: 20 passed, 0 failed)

| Page / flow | Spec | Baseline | Upgraded | Pixel diff |
|---|---|---|---|---|
| Login form (heading, inputs, Remember me, Login enabled) | login.spec.ts | pass | pass | 0.00% (`01-login`) |
| Admin sign-in shows welcome + menu | login.spec.ts | pass | pass | 0.00% (`02-welcome`) |
| Wrong password flips to welcome but loads no data (legacy bug, kept) | login.spec.ts | pass | pass | - |
| Logout ends backend session, menu hidden | logout.spec.ts | pass | pass | 0.00% (`08-after-logout`) |
| User Account list with balances | user-account.spec.ts | pass | pass | 0.00% (`03-user-account`) |
| Disable / re-enable a user | user-account.spec.ts | pass | pass | 0.07% (`04-user-disabled`) |
| User list empty without a session | user-account.spec.ts | pass | pass | - |
| Primary account transactions drill-down | transactions.spec.ts | pass | pass | 0.00% (`05-primary-transactions`) |
| Savings account transactions drill-down | transactions.spec.ts | pass | pass | 0.00% (`06-savings-transactions`) |
| Appointment list + confirm | appointment.spec.ts | pass | pass | 0.02% (`07-appointments`) |

Mismatches: none in UI. The two non-zero diffs are data, not rendering: both projects share one database and each project toggles/confirms its own seeded row, so the "Enabled"/"Confirmed?" cell of the *other* project's row differs between the two screenshots (see `side-by-side/04-user-disabled.png`, `side-by-side/07-appointments.png`).

- `screens/baseline/*.png`, `screens/upgraded/*.png`: full-page screenshots per flow.
- `side-by-side/*.png`: baseline left, upgraded right.
- `pixel-diff.json`: pixelmatch (threshold 0.1) % of changed pixels.

## Re-run

```bash
# MySQL 5.7 with the onlinebanking schema (password from UserFront application.properties)
docker run -d --name obwa-mysql -p 127.0.0.1:3306:3306 -e MYSQL_ROOT_PASSWORD=<db password> -e MYSQL_DATABASE=onlinebanking mysql:5.7
# Backend (Java 8)
cd UserFront && mvn -DskipTests package && java -jar target/userFront-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url='jdbc:mysql://127.0.0.1:3306/onlinebanking?useSSL=false' \
  --cors.allowed-origins=http://localhost:4200,http://localhost:4000
# Baseline (Node 12): git worktree add ../obwa-baseline master
cd ../obwa-baseline/AdminPortal && npm install && npx ng serve --port 4000   # master has no lockfile
# Upgraded (Node >= 20.19 / 22.12)
cd AdminPortal && npm ci && npx ng serve --port 4200
# Suite (needs the mysql client on PATH; PARITY_DB_* env vars override host/port/user/db/password)
cd parity && npm ci && npx playwright install chromium
PARITY_RESET_DB=1 npx playwright test   # both projects; --project=baseline|upgraded for one
npm run compare                          # side-by-side + pixel-diff.json
```
