import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Transaction, User } from './models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);

  getUsers(): Observable<User[]> {
    const url = 'http://localhost:8080/api/user/all';
    return this.http.get<User[]>(url, { withCredentials: true });
  }

  getPrimaryTransactionList(username: string): Observable<Transaction[]> {
    const url = 'http://localhost:8080/api/user/primary/transaction?username=' + username;
    return this.http.get<Transaction[]>(url, { withCredentials: true });
  }

  getSavingsTransactionList(username: string): Observable<Transaction[]> {
    const url = 'http://localhost:8080/api/user/savings/transaction?username=' + username;
    return this.http.get<Transaction[]>(url, { withCredentials: true });
  }

  enableUser(username: string): Observable<unknown> {
    const url = 'http://localhost:8080/api/user/' + username + '/enable';
    return this.http.get(url, { withCredentials: true });
  }

  disableUser(username: string): Observable<unknown> {
    const url = 'http://localhost:8080/api/user/' + username + '/disable';
    return this.http.get(url, { withCredentials: true });
  }
}
