import { Injectable } from '@angular/core';
import {Http, Headers} from '@angular/http';
import {Observable}     from 'rxjs/Observable';
import 'rxjs/add/operator/mergeMap';

@Injectable()
export class LoginService {

  constructor (private http: Http) {}

  sendCredential(username: string, password: string) {
    let url = 'http://localhost:8080/index';
    let params = 'username='+username+'&password='+password;
    let headers = new Headers(
    {
      'Content-Type': 'application/x-www-form-urlencoded'
      // 'Access-Control-Allow-Credentials' : true
    });

    // The XSRF-TOKEN cookie has to be issued by the server before the login POST
    // can be sent, otherwise the request is rejected by the CSRF filter.
    return this.http.get('http://localhost:8080/api/csrf', { withCredentials: true })
      .mergeMap(() => this.http.post(url, params, {headers: headers, withCredentials : true}));
  }

  logout() {
     let url = 'http://localhost:8080/logout';
     return this.http.get(url, { withCredentials: true });
   }

}
