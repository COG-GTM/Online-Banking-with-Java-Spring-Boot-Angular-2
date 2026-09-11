import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

export interface Account {
  accountNumber: number;
  accountBalance: number;
}

export interface User {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  enabled: boolean;
  primaryAccount: Account;
  savingsAccount: Account;
}

export interface Transaction {
  date: string;
  description: string;
  type: string;
  status: string;
  amount: number;
  availableBalance: number;
}

@Injectable()
export class UserService {

  constructor(private http: HttpClient) {}

  getUsers(): Observable<User[]> {
    let url = "http://localhost:8080/api/user/all";
    return this.http.get<User[]>(url, { withCredentials: true });
  }

   getPrimaryTransactionList(username: string): Observable<Transaction[]> {
     let url = "http://localhost:8080/api/user/primary/transaction?username="+username;
    return this.http.get<Transaction[]>(url, { withCredentials: true });
   }

   getSavingsTransactionList(username: string): Observable<Transaction[]> {
     let url = "http://localhost:8080/api/user/savings/transaction?username="+username;
    return this.http.get<Transaction[]>(url, { withCredentials: true });
   }

   enableUser(username: string): Observable<string> {
     let url = "http://localhost:8080/api/user/"+username+"/enable";
     return this.http.get(url, { withCredentials: true, responseType: 'text' });
   }

   disableUser(username: string): Observable<string> {
     let url = "http://localhost:8080/api/user/"+username+"/disable";
     return this.http.get(url, { withCredentials: true, responseType: 'text' });
   }

}
