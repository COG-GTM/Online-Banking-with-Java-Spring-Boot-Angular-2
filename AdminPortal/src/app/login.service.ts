import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { environment } from '../environments/environment';

@Injectable()
export class LoginService {

  private baseUrl = environment.apiUrl;

  constructor (private http: HttpClient) {}

  sendCredential(username: string, password: string): Observable<any> {
    let url = this.baseUrl + '/index';
    let params = 'username=' + username + '&password=' + password;
    let headers = new HttpHeaders({
      'Content-Type': 'application/x-www-form-urlencoded'
    });
    return this.http.post(url, params, { headers: headers, withCredentials: true, responseType: 'text' });
  }

  logout(): Observable<any> {
     let url = this.baseUrl + '/logout';
     return this.http.get(url, { withCredentials: true, responseType: 'text' });
   }

}
