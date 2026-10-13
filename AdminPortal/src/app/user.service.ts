import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { Transaction, User } from './models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/user`;

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.baseUrl}/all`, { withCredentials: true });
  }

  getPrimaryTransactionList(username: string): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.baseUrl}/primary/transaction`, {
      params: new HttpParams().set('username', username),
      withCredentials: true,
    });
  }

  getSavingsTransactionList(username: string): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.baseUrl}/savings/transaction`, {
      params: new HttpParams().set('username', username),
      withCredentials: true,
    });
  }

  enableUser(username: string): Observable<string> {
    return this.http.get(`${this.baseUrl}/${encodeURIComponent(username)}/enable`, {
      withCredentials: true,
      responseType: 'text',
    });
  }

  disableUser(username: string): Observable<string> {
    return this.http.get(`${this.baseUrl}/${encodeURIComponent(username)}/disable`, {
      withCredentials: true,
      responseType: 'text',
    });
  }
}
