import { Injectable } from '@angular/core';
import {Http, Headers} from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/mergeMap';
import { withXsrfHeader } from './xsrf';

@Injectable()
export class LoginService {

  constructor (private http: Http) {}

  sendCredential(username: string, password: string) {
    const url = 'http://localhost:8080/index';
    const params = 'username=' + username + '&password=' + password;
    // Prime the XSRF-TOKEN cookie on a cold browser before posting credentials.
    return this.http.get(url, { withCredentials: true })
      .catch(() => Observable.of(null))
      .mergeMap(() => {
        const headers = withXsrfHeader(new Headers({ 'Content-Type': 'application/x-www-form-urlencoded' }));
        return this.http.post(url, params, { headers: headers, withCredentials: true });
      });
  }

  logout() {
     const url = 'http://localhost:8080/logout';
     return this.http.get(url, { headers: withXsrfHeader(), withCredentials: true });
   }

}
