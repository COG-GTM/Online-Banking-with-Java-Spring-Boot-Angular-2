import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export const LOGGED_IN_KEY = 'PortalAdminHasLoggedIn';

@Injectable({ providedIn: 'root' })
export class LoginService {
  private readonly http = inject(HttpClient);

  sendCredential(username: string, password: string): Observable<string> {
    const body = new HttpParams().set('username', username).set('password', password);
    const headers = new HttpHeaders({ 'Content-Type': 'application/x-www-form-urlencoded' });
    return this.http.post(`${environment.apiUrl}/index`, body.toString(), {
      headers,
      withCredentials: true,
      responseType: 'text',
    });
  }

  logout(): Observable<string> {
    return this.http.get(`${environment.apiUrl}/logout`, {
      withCredentials: true,
      responseType: 'text',
    });
  }

  isLoggedIn(): boolean {
    const value = localStorage.getItem(LOGGED_IN_KEY);
    return value !== null && value !== '';
  }
}
