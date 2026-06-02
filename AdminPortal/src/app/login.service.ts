import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../environments/environment';

@Injectable({ providedIn: 'root' })
export class LoginService {

  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  sendCredential(username: string, password: string): Observable<string> {
    const url = `${this.baseUrl}/index`;
    const params = `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`;
    const headers = new HttpHeaders({
      'Content-Type': 'application/x-www-form-urlencoded'
    });
    return this.http.post(url, params, {
      headers,
      withCredentials: true,
      responseType: 'text'
    });
  }

  logout(): Observable<string> {
    const url = `${this.baseUrl}/logout`;
    return this.http.get(url, { withCredentials: true, responseType: 'text' });
  }
}
