import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Observable } from 'rxjs/Observable';

const AUTH_KEY = 'PortalAdminHasLoggedIn';

@Injectable()
export class AuthService {

  private loggedIn = new BehaviorSubject<boolean>(this.hasStoredLogin());

  isLoggedIn$: Observable<boolean> = this.loggedIn.asObservable();

  get isLoggedIn(): boolean {
    return this.loggedIn.value;
  }

  setLoggedIn(value: boolean) {
    if (value) {
      localStorage.setItem(AUTH_KEY, 'true');
    } else {
      localStorage.removeItem(AUTH_KEY);
    }
    this.loggedIn.next(value);
  }

  private hasStoredLogin(): boolean {
    const value = localStorage.getItem(AUTH_KEY);
    return value != null && value !== '';
  }

}
