import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class LoginService {

  constructor (private http: HttpClient) {}

  sendCredential(username: string, password: string): Observable<string> {
    let url = 'http://localhost:8080/index';
    let params = 'username=' + username + '&password=' + password;
    let headers = new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded');
    return this.http.post(url, params, { headers: headers, withCredentials: true, responseType: 'text' });
  }

  logout(): Observable<string> {
    let url = 'http://localhost:8080/logout';
    return this.http.get(url, { withCredentials: true, responseType: 'text' });
   }

}
