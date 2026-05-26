import { Injectable } from '@angular/core';
import { Http } from '@angular/http';

@Injectable()
export class TransactionService {

  constructor(private http: Http) {}

  getPrimaryTransactionList(username: string) {
    let url = "http://localhost:8080/api/user/primary/transaction?username=" + username;
    return this.http.get(url, { withCredentials: true });
  }

  getSavingsTransactionList(username: string) {
    let url = "http://localhost:8080/api/user/savings/transaction?username=" + username;
    return this.http.get(url, { withCredentials: true });
  }
}
