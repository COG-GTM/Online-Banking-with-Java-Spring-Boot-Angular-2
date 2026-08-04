import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class UserService {

  constructor(private http: HttpClient) {}

  getUsers(): Observable<Object[]> {
    const url = 'http://localhost:8080/api/user/all';
    return this.http.get<Object[]>(url, { withCredentials: true });
  }

  getPrimaryTransactionList(username: string): Observable<Object[]> {
    const url = 'http://localhost:8080/api/user/primary/transaction?username=' + username;
    return this.http.get<Object[]>(url, { withCredentials: true });
  }

  getSavingsTransactionList(username: string): Observable<Object[]> {
    const url = 'http://localhost:8080/api/user/savings/transaction?username=' + username;
    return this.http.get<Object[]>(url, { withCredentials: true });
  }

  enableUser(username: string): Observable<string> {
    const url = 'http://localhost:8080/api/user/' + username + '/enable';
    return this.http.get(url, { withCredentials: true, responseType: 'text' });
  }

  disableUser(username: string): Observable<string> {
    const url = 'http://localhost:8080/api/user/' + username + '/disable';
    return this.http.get(url, { withCredentials: true, responseType: 'text' });
  }

}
