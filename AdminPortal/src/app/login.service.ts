import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../environments/environment';

@Injectable({ providedIn: 'root' })
export class LoginService {

  constructor(private http: HttpClient) {}

  sendCredential(username: string, password: string): Observable<string> {
    const url = `${environment.apiBaseUrl}/index`;
    // Credentials are sent in the (encoded) request body, never in the URL.
    const body = 'username=' + encodeURIComponent(username)
               + '&password=' + encodeURIComponent(password);
    const headers = new HttpHeaders({
      'Content-Type': 'application/x-www-form-urlencoded',
    });
    return this.http.post(url, body, {
      headers,
      withCredentials: true,
      responseType: 'text',
    });
  }

  logout(): Observable<string> {
    const url = `${environment.apiBaseUrl}/logout`;
    return this.http.get(url, { withCredentials: true, responseType: 'text' });
  }
}
