import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { User } from './models';
import { UserService } from './user.service';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  function expectGet(url: string) {
    const req = httpMock.expectOne(url);
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    return req;
  }

  it('GETs /api/user/all and returns parsed JSON', () => {
    const users = [{ username: 'jdoe' }] as User[];
    let result: User[] | undefined;
    service.getUsers().subscribe((res) => (result = res));

    expectGet('http://localhost:8080/api/user/all').flush(users);
    expect(result).toEqual(users);
  });

  it('GETs primary transactions by username', () => {
    service.getPrimaryTransactionList('jdoe').subscribe();
    expectGet('http://localhost:8080/api/user/primary/transaction?username=jdoe').flush([]);
  });

  it('GETs savings transactions by username', () => {
    service.getSavingsTransactionList('jdoe').subscribe();
    expectGet('http://localhost:8080/api/user/savings/transaction?username=jdoe').flush([]);
  });

  it('GETs /api/user/{username}/enable', () => {
    service.enableUser('jdoe').subscribe();
    expectGet('http://localhost:8080/api/user/jdoe/enable').flush(null);
  });

  it('GETs /api/user/{username}/disable', () => {
    service.disableUser('jdoe').subscribe();
    expectGet('http://localhost:8080/api/user/jdoe/disable').flush(null);
  });
});
