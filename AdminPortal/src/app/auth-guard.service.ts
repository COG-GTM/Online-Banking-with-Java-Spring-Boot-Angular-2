import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { Http } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/of';

/**
 * Route guard that verifies the admin actually has a valid server-side session
 * before activating a protected route. Authorization is confirmed by calling a
 * secured API endpoint rather than trusting the client-only `localStorage` flag,
 * which a user could set manually.
 */
@Injectable()
export class AuthGuard implements CanActivate {

  constructor(private http: Http, private router: Router) {}

  canActivate(): Observable<boolean> {
    return this.http.get('http://localhost:8080/api/user/all', { withCredentials: true })
      .map(() => true)
      .catch(() => {
        localStorage.removeItem('PortalAdminHasLoggedIn');
        this.router.navigate(['/login']);
        return Observable.of(false);
      });
  }
}
