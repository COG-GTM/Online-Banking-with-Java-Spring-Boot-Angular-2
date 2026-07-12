import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { map, catchError, of } from 'rxjs';

import { environment } from '../environments/environment';

/**
 * Route guard that verifies the admin actually has a valid server-side session
 * before activating a protected route. Authorization is confirmed by calling a
 * secured API endpoint rather than trusting the client-only `localStorage` flag,
 * which a user could set manually.
 */
export const authGuard: CanActivateFn = () => {
  const http = inject(HttpClient);
  const router = inject(Router);

  return http.get(`${environment.apiBaseUrl}/api/user/all`, { withCredentials: true }).pipe(
    map(() => true),
    catchError(() => {
      localStorage.removeItem('PortalAdminHasLoggedIn');
      router.navigate(['/login']);
      return of(false);
    }),
  );
};
