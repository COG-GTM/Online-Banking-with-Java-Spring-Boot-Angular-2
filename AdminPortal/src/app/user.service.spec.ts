import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { UserService } from './user.service';
import { environment } from '../environments/environment';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [UserService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('fetches all users with credentials', () => {
    service.getUsers().subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/api/user/all`);
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBeTrue();
    req.flush([]);
  });

  it('passes username as a path variable, not a query string', () => {
    service.getPrimaryTransactionList('bob smith').subscribe();
    const req = httpMock.expectOne(
      `${environment.apiBaseUrl}/api/user/primary/transaction/bob%20smith`,
    );
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('enables a user via POST', () => {
    service.enableUser('bob').subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/api/user/bob/enable`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });
});
