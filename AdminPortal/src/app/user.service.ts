import { Injectable } from '@angular/core';
import {Http, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/publishReplay';

export const LIST_CACHE_TTL_MS = 30000;

const USERS_URL = 'http://localhost:8080/api/user/all';

interface CacheEntry {
  expiresAt: number;
  response: Observable<Response>;
}

@Injectable()
export class UserService {

  private listCache: { [url: string]: CacheEntry } = {};

  constructor (private http:Http){}

  getUsers() {
    return this.cachedGet(USERS_URL);
  }

   getPrimaryTransactionList(username: string) {
     let url = "http://localhost:8080/api/user/primary/transaction?username="+username;
    return this.cachedGet(url);
   }

   getSavingsTransactionList(username: string) {
     let url = "http://localhost:8080/api/user/savings/transaction?username="+username;
    return this.cachedGet(url);
   }

   enableUser (username: string) {
     let url = "http://localhost:8080/api/user/"+username+"/enable";
     return this.http.get(url, { withCredentials: true })
       .do(() => this.invalidateUsers(), () => this.invalidateUsers());
   }

   disableUser (username: string) {
     let url = "http://localhost:8080/api/user/"+username+"/disable";
     return this.http.get(url, { withCredentials: true })
       .do(() => this.invalidateUsers(), () => this.invalidateUsers());
   }

   invalidateUsers() {
     delete this.listCache[USERS_URL];
   }

   private cachedGet(url: string): Observable<Response> {
     const now = Date.now();
     const entry = this.listCache[url];
     if (entry && entry.expiresAt > now) {
       return entry.response;
     }
     this.pruneExpired(now);
     const response = this.http.get(url, { withCredentials: true })
       .do(null, () => {
         if (this.listCache[url] && this.listCache[url].response === response) {
           delete this.listCache[url];
         }
       })
       .publishReplay(1)
       .refCount();
     this.listCache[url] = { expiresAt: now + LIST_CACHE_TTL_MS, response: response };
     return response;
   }

   private pruneExpired(now: number) {
     Object.keys(this.listCache).forEach(url => {
       if (this.listCache[url].expiresAt <= now) {
         delete this.listCache[url];
       }
     });
   }

}
