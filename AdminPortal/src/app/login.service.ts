import { Injectable } from '@angular/core';
import {Http, Headers} from '@angular/http';
import {Observable}     from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/mergeMap';
import { xsrfOptions } from './xsrf';

@Injectable()
export class LoginService {

  constructor (private http: Http) {}

  sendCredential(username: string, password: string) {
    let url = 'http://localhost:8080/index';
    let params = 'username='+username+'&password='+password;
    return this.bootstrapXsrfCookie().mergeMap(() => {
      const headers = new Headers(
      {
        'Content-Type': 'application/x-www-form-urlencoded'
        // 'Access-Control-Allow-Credentials' : true
      });
      return this.http.post(url, params, xsrfOptions(headers));
    });
  }

  // Ensures the XSRF-TOKEN cookie exists on a cold browser before the login POST.
  bootstrapXsrfCookie() {
    const url = 'http://localhost:8080/index';
    return this.http.get(url, { withCredentials: true }).catch(() => Observable.of(null));
  }

  logout() {
     let url = 'http://localhost:8080/logout';
     return this.http.get(url, { withCredentials: true });
   }

}
