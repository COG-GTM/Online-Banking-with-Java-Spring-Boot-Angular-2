import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { UserService } from './user.service';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('returns parsed users', () => {
    let result: unknown;
    service.getUsers().subscribe((users) => (result = users));
    const req = httpMock.expectOne('http://localhost:8080/api/user/all');
    expect(req.request.withCredentials).toBe(true);
    req.flush([{ username: 'jdoe' }]);
    expect(result).toEqual([{ username: 'jdoe' }]);
  });

  it('requests primary and savings transactions by username', () => {
    service.getPrimaryTransactionList('jdoe').subscribe();
    service.getSavingsTransactionList('jdoe').subscribe();
    httpMock
      .expectOne('http://localhost:8080/api/user/primary/transaction?username=jdoe')
      .flush([]);
    httpMock
      .expectOne('http://localhost:8080/api/user/savings/transaction?username=jdoe')
      .flush([]);
  });

  it('enables and disables users', () => {
    service.enableUser('jdoe').subscribe();
    service.disableUser('jdoe').subscribe();
    httpMock.expectOne('http://localhost:8080/api/user/jdoe/enable').flush('');
    httpMock.expectOne('http://localhost:8080/api/user/jdoe/disable').flush('');
  });
});
