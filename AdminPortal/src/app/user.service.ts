import { Injectable } from '@angular/core';
import {Http, Headers} from '@angular/http';


@Injectable()
export class UserService {

  constructor (private http:Http){}

  private static usernamePathSegment(username: string): string {
    if (username === '.' || username === '..') {
      throw new Error('Invalid username: ' + username);
    }
    return encodeURIComponent(username);
  }

  getUsers() {
    let url = "http://localhost:8080/api/user/all";
    return this.http.get(url, { withCredentials: true });
  }

   getPrimaryTransactionList(username: string) {
     let url = "http://localhost:8080/api/user/primary/transaction?username="+encodeURIComponent(username);
    return this.http.get(url, { withCredentials: true });
   }

   getSavingsTransactionList(username: string) {
     let url = "http://localhost:8080/api/user/savings/transaction?username="+encodeURIComponent(username);
    return this.http.get(url, { withCredentials: true });
   }

   enableUser (username: string) {
     let url = "http://localhost:8080/api/user/"+UserService.usernamePathSegment(username)+"/enable";
     return this.http.get(url, { withCredentials: true });
   }

   disableUser (username: string) {
     let url = "http://localhost:8080/api/user/"+UserService.usernamePathSegment(username)+"/disable";
     return this.http.get(url, { withCredentials: true });
   }

}
