import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../environments/environment';
import { Transaction, User } from './models';

@Injectable({ providedIn: 'root' })
export class UserService {

  constructor(private http: HttpClient) {}

  getUsers(): Observable<User[]> {
    const url = `${environment.apiBaseUrl}/api/user/all`;
    return this.http.get<User[]>(url, { withCredentials: true });
  }

  getPrimaryTransactionList(username: string): Observable<Transaction[]> {
    const url = `${environment.apiBaseUrl}/api/user/primary/transaction/`
      + encodeURIComponent(username);
    return this.http.get<Transaction[]>(url, { withCredentials: true });
  }

  getSavingsTransactionList(username: string): Observable<Transaction[]> {
    const url = `${environment.apiBaseUrl}/api/user/savings/transaction/`
      + encodeURIComponent(username);
    return this.http.get<Transaction[]>(url, { withCredentials: true });
  }

  enableUser(username: string): Observable<unknown> {
    const url = `${environment.apiBaseUrl}/api/user/`
      + encodeURIComponent(username) + '/enable';
    return this.http.post(url, {}, { withCredentials: true });
  }

  disableUser(username: string): Observable<unknown> {
    const url = `${environment.apiBaseUrl}/api/user/`
      + encodeURIComponent(username) + '/disable';
    return this.http.post(url, {}, { withCredentials: true });
  }
}
