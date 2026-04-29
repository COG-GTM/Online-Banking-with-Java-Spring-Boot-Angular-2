import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Injectable()
export class LoginService {
  constructor(private http: HttpClient) {}

  sendCredential(username: string, password: string) {
    const url = 'http://localhost:8080/index';
    const body = 'username=' + username + '&password=' + password;
    const headers = new HttpHeaders({ 'Content-Type': 'application/x-www-form-urlencoded' });
    return this.http.post(url, body, { headers, withCredentials: true, responseType: 'text' });
  }

  logout() {
    const url = 'http://localhost:8080/logout';
    return this.http.get(url, { withCredentials: true, responseType: 'text' });
  }
}
