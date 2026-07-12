import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { Observable } from 'rxjs';

import { authGuard } from './auth.guard';
import { environment } from '../environments/environment';

describe('authGuard', () => {
  let httpMock: HttpTestingController;
  let router: jasmine.SpyObj<Router>;

  const runGuard = (): Observable<boolean> =>
    TestBed.runInInjectionContext(
      () =>
        authGuard(
          {} as ActivatedRouteSnapshot,
          {} as RouterStateSnapshot,
        ) as Observable<boolean>,
    );

  beforeEach(() => {
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: Router, useValue: router },
      ],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('allows activation when the session endpoint succeeds', (done) => {
    runGuard().subscribe((allowed) => {
      expect(allowed).toBeTrue();
      expect(router.navigate).not.toHaveBeenCalled();
      done();
    });
    httpMock.expectOne(`${environment.apiBaseUrl}/api/user/all`).flush([]);
  });

  it('blocks activation and redirects to /login when unauthenticated', (done) => {
    runGuard().subscribe((allowed) => {
      expect(allowed).toBeFalse();
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
      done();
    });
    httpMock
      .expectOne(`${environment.apiBaseUrl}/api/user/all`)
      .flush('nope', { status: 401, statusText: 'Unauthorized' });
  });
});
