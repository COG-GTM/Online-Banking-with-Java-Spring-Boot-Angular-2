# SNYK-JS-ANGULARCOMMON-17356555 / CVE-2026-54266 (Use of Weak Hash)

**Package:** `@angular/common@4.4.7` (direct dependency of `AdminPortal`)
**Snyk fixed versions:** 20.3.25, 21.2.17, 22.0.1

## Why this cannot be fully remediated in place

- The vulnerability is a 32-bit hash collision in `HttpTransferCache`, the
  SSR HTTP transfer cache introduced in Angular 17. `@angular/common@4.4.7`
  contains no `HttpTransferCache`/`TransferState` code, and `AdminPortal`
  does not render on the server (`@angular/platform-server` is not a
  dependency), so the vulnerable code path is not present in this app.
- Angular 4.x reached end of life in 2018; `4.4.7` is the last 4.x release.
  There is no backported fix in any 4.x, 5.x or later pre-20 line.
- Upgrading to `@angular/common@>=20.3.25` requires moving the whole
  AdminPortal (Angular 4.4.7, Angular CLI 1.1.2, TypeScript 2.3, RxJS 5,
  `@angular/http`) to Angular 20+, which is a framework rewrite and out of
  scope for a single dependency fix.

## Mitigation applied

- `@angular/common` is pinned to `4.4.7` (already the resolved version) so
  the resolved version is explicit.
- `AdminPortal/.snyk` records a time-boxed risk acceptance for
  `SNYK-JS-ANGULARCOMMON-17356555` with the reachability rationale above.
  The ignore expires on 2027-03-11 so it is re-evaluated.

## Residual risk / follow-up

Snyk still matches the other `@angular/common@4.4.7` findings
(`SNYK-JS-ANGULARCOMMON-14135651`, `-17353316`, `-17356486`, `-17356499`,
`-18550632`), which are also only fixed in Angular 19/20+. The real fix is a
staged Angular upgrade of AdminPortal (4 -> 8 -> 12 -> 16 -> 20 via
`ng update`), replacing `@angular/http` with `@angular/common/http` and
`rxjs-compat` where needed.
