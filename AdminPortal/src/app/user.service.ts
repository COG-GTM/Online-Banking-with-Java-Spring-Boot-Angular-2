import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { environment } from '../environments/environment';


@Injectable()
export class UserService {

  private baseUrl = environment.apiUrl;

  constructor (private http: HttpClient) {}

  getUsers(): Observable<any> {
    let url = this.baseUrl + '/api/user/all';
    return this.http.get(url, { withCredentials: true });
  }

   getPrimaryTransactionList(username: string): Observable<any> {
     let url = this.baseUrl + '/api/user/primary/transaction?username=' + username;
    return this.http.get(url, { withCredentials: true });
   }

   getSavingsTransactionList(username: string): Observable<any> {
     let url = this.baseUrl + '/api/user/savings/transaction?username=' + username;
    return this.http.get(url, { withCredentials: true });
   }

   enableUser (username: string): Observable<any> {
     let url = this.baseUrl + '/api/user/' + username + '/enable';
     return this.http.get(url, { withCredentials: true });
   }

   disableUser (username: string): Observable<any> {
     let url = this.baseUrl + '/api/user/' + username + '/disable';
     return this.http.get(url, { withCredentials: true });
   }

}
