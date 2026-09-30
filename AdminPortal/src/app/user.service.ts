import { Injectable } from '@angular/core';
import {Http, Headers} from '@angular/http';


@Injectable()
export class UserService {

  constructor (private http:Http){}

  getUsers() {
    let url = "http://localhost:8080/api/user/all";
    return this.http.get(url, { withCredentials: true });
  }

   getPrimaryTransactionList(username: string) {
     let url = "http://localhost:8080/api/user/primary/transaction?username="+username;
    return this.http.get(url, { withCredentials: true });
   }

   getSavingsTransactionList(username: string) {
     let url = "http://localhost:8080/api/user/savings/transaction?username="+username;
    return this.http.get(url, { withCredentials: true });
   }

   enableUser (username: string) {
     let url = "http://localhost:8080/api/user/"+username+"/enable";
     return this.http.post(url, null, { headers: this.xsrfHeaders(), withCredentials: true });
   }

   disableUser (username: string) {
     let url = "http://localhost:8080/api/user/"+username+"/disable";
     return this.http.post(url, null, { headers: this.xsrfHeaders(), withCredentials: true });
   }

   private xsrfHeaders(): Headers {
     const headers = new Headers();
     const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);
     if (match) {
       headers.append('X-XSRF-TOKEN', decodeURIComponent(match[1]));
     }
     return headers;
   }

}
