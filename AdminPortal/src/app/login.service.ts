import { Injectable } from '@angular/core';
import {Http, Headers} from '@angular/http';
import {Observable}     from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/mergeMap';
import {xsrfOptions} from './xsrf';

@Injectable()
export class LoginService {

  constructor (private http: Http) {}

  sendCredential(username: string, password: string) {
    let url = 'http://localhost:8080/index';
    let params = 'username='+username+'&password='+password;
    return this.fetchCsrfToken().mergeMap(() => {
      let headers = new Headers(
      {
        'Content-Type': 'application/x-www-form-urlencoded'
        // 'Access-Control-Allow-Credentials' : true
      });
      return this.http.post(url, params, xsrfOptions(headers));
    });
  }

  logout() {
     let url = 'http://localhost:8080/logout';
     return this.http.get(url, { withCredentials: true });
   }

  // GET /index is permitAll, so unlike a protected URL it is not stored as the post-login redirect target.
  private fetchCsrfToken(): Observable<any> {
    const url = 'http://localhost:8080/index';
    return this.http.get(url, { withCredentials: true }).catch(() => Observable.of(null));
  }

}
