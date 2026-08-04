import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class LoginService {

  constructor(private http: HttpClient) {}

  sendCredential(username: string, password: string): Observable<string> {
    const url = 'http://localhost:8080/index';
    const params = 'username=' + username + '&password=' + password;
    const headers = new HttpHeaders({
      'Content-Type': 'application/x-www-form-urlencoded'
    });
    return this.http.post(url, params, { headers: headers, withCredentials: true, responseType: 'text' });
  }

  logout(): Observable<string> {
    const url = 'http://localhost:8080/logout';
    return this.http.get(url, { withCredentials: true, responseType: 'text' });
  }

}
