import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../environments/environment';

@Injectable({ providedIn: 'root' })
export class UserService {

  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getUsers(): Observable<any[]> {
    const url = `${this.baseUrl}/api/user/all`;
    return this.http.get<any[]>(url, { withCredentials: true });
  }

  getPrimaryTransactionList(username: string): Observable<any[]> {
    const url = `${this.baseUrl}/api/user/primary/transaction?username=${encodeURIComponent(username)}`;
    return this.http.get<any[]>(url, { withCredentials: true });
  }

  getSavingsTransactionList(username: string): Observable<any[]> {
    const url = `${this.baseUrl}/api/user/savings/transaction?username=${encodeURIComponent(username)}`;
    return this.http.get<any[]>(url, { withCredentials: true });
  }

  enableUser(username: string): Observable<void> {
    const url = `${this.baseUrl}/api/user/${encodeURIComponent(username)}/enable`;
    return this.http.put<void>(url, {}, { withCredentials: true });
  }

  disableUser(username: string): Observable<void> {
    const url = `${this.baseUrl}/api/user/${encodeURIComponent(username)}/disable`;
    return this.http.put<void>(url, {}, { withCredentials: true });
  }
}
