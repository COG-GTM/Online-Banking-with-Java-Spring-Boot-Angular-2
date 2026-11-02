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
    // GET first so the backend issues the XSRF-TOKEN cookie; Http's CookieXSRFStrategy
    // then sends it back as the X-XSRF-TOKEN header on the login POST.
    return this.http.get(url, { withCredentials: true })
      .mergeMap(() => this.http.post(url, params, {headers: headers, withCredentials : true}));
  }

  logout() {
     let url = 'http://localhost:8080/logout';
     return this.http.post(url, '', { withCredentials: true });
   }

}
