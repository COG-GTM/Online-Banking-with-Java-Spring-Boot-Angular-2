import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { UserService } from './user.service';

describe('Service: User', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch all users', () => {
    service.getUsers().subscribe();
    const req = httpMock.expectOne('http://localhost:8080/api/user/all');
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBeTrue();
    req.flush([]);
  });

  it('should fetch transaction lists for an encoded username', () => {
    service.getPrimaryTransactionList('a b').subscribe();
    service.getSavingsTransactionList('a b').subscribe();
    httpMock.expectOne('http://localhost:8080/api/user/primary/transaction?username=a%20b').flush([]);
    httpMock.expectOne('http://localhost:8080/api/user/savings/transaction?username=a%20b').flush([]);
  });

  it('should enable and disable users with GET', () => {
    service.enableUser('alice').subscribe();
    service.disableUser('alice').subscribe();
    const enable = httpMock.expectOne('http://localhost:8080/api/user/alice/enable');
    const disable = httpMock.expectOne('http://localhost:8080/api/user/alice/disable');
    expect(enable.request.method).toBe('GET');
    expect(disable.request.method).toBe('GET');
    enable.flush(null);
    disable.flush(null);
  });
});
