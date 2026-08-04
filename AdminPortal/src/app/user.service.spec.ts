import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { UserService } from './user.service';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UserService]
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should request the user list with credentials', () => {
    const users = [{ username: 'admin' }];
    let result: object[] | undefined;

    service.getUsers().subscribe(res => (result = res));

    const req = httpMock.expectOne('http://localhost:8080/api/user/all');
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush(users);

    expect(result).toEqual(users);
  });

  it('should request the primary transaction list for a user', () => {
    service.getPrimaryTransactionList('john').subscribe();

    const req = httpMock.expectOne(
      'http://localhost:8080/api/user/primary/transaction?username=john'
    );
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should request the savings transaction list for a user', () => {
    service.getSavingsTransactionList('john').subscribe();

    const req = httpMock.expectOne(
      'http://localhost:8080/api/user/savings/transaction?username=john'
    );
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should enable and disable a user', () => {
    service.enableUser('john').subscribe();
    const enableReq = httpMock.expectOne('http://localhost:8080/api/user/john/enable');
    expect(enableReq.request.method).toBe('GET');
    enableReq.flush('');

    service.disableUser('john').subscribe();
    const disableReq = httpMock.expectOne('http://localhost:8080/api/user/john/disable');
    expect(disableReq.request.method).toBe('GET');
    disableReq.flush('');
  });
});
