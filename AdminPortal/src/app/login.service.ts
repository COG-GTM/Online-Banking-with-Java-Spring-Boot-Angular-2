import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export const LOGGED_IN_KEY = 'PortalAdminHasLoggedIn';

@Injectable({ providedIn: 'root' })
export class LoginService {
  private readonly http = inject(HttpClient);

  readonly loggedIn = signal(!!localStorage.getItem(LOGGED_IN_KEY));

  sendCredential(username: string, password: string): Observable<string> {
    const url = 'http://localhost:8080/index';
    const params = new URLSearchParams({ username, password }).toString();
    const headers = new HttpHeaders({
      'Content-Type': 'application/x-www-form-urlencoded',
    });
    return this.http.post(url, params, { headers, withCredentials: true, responseType: 'text' });
  }

  logout(): Observable<string> {
    const url = 'http://localhost:8080/logout';
    return this.http.get(url, { withCredentials: true, responseType: 'text' });
  }

  markLoggedIn(): void {
    localStorage.setItem(LOGGED_IN_KEY, 'true');
    this.loggedIn.set(true);
  }

  markLoggedOut(): void {
    localStorage.setItem(LOGGED_IN_KEY, '');
    this.loggedIn.set(false);
  }
}
