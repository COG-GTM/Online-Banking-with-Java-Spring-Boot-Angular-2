/* tslint:disable:no-unused-variable */

import { TestBed, inject } from '@angular/core/testing';
import { HttpModule, XHRBackend, RequestMethod, ResponseOptions, Response } from '@angular/http';
import { MockBackend, MockConnection } from '@angular/http/testing';

import { UserService } from './user.service';

describe('Service: User', () => {
  let backend: MockBackend;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpModule],
      providers: [
        UserService,
        { provide: XHRBackend, useClass: MockBackend }
      ]
    });
  });

  beforeEach(inject([XHRBackend], (mockBackend: MockBackend) => {
    backend = mockBackend;
  }));

  function expectGet(url: string, assertConnection: (conn: MockConnection) => void) {
    backend.connections.subscribe((conn: MockConnection) => {
      expect(conn.request.method).toBe(RequestMethod.Get);
      expect(conn.request.url).toBe(url);
      expect(conn.request.withCredentials).toBe(true);
      assertConnection(conn);
      conn.mockRespond(new Response(new ResponseOptions({ body: '[]' })));
    });
  }

  it('should be created', inject([UserService], (service: UserService) => {
    expect(service).toBeTruthy();
  }));

  it('getUsers should GET /api/user/all with credentials',
    inject([UserService], (service: UserService) => {
      expectGet('http://localhost:8080/api/user/all', () => {});
      service.getUsers().subscribe();
  }));

  it('getPrimaryTransactionList should GET the primary transaction endpoint with the username query param',
    inject([UserService], (service: UserService) => {
      expectGet('http://localhost:8080/api/user/primary/transaction?username=alice', () => {});
      service.getPrimaryTransactionList('alice').subscribe();
  }));

  it('getSavingsTransactionList should GET the savings transaction endpoint with the username query param',
    inject([UserService], (service: UserService) => {
      expectGet('http://localhost:8080/api/user/savings/transaction?username=bob', () => {});
      service.getSavingsTransactionList('bob').subscribe();
  }));

  it('enableUser should GET /api/user/{username}/enable with credentials',
    inject([UserService], (service: UserService) => {
      expectGet('http://localhost:8080/api/user/alice/enable', () => {});
      service.enableUser('alice').subscribe();
  }));

  it('disableUser should GET /api/user/{username}/disable with credentials',
    inject([UserService], (service: UserService) => {
      expectGet('http://localhost:8080/api/user/alice/disable', () => {});
      service.disableUser('alice').subscribe();
  }));
});
