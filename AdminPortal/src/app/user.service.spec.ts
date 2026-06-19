import { TestBed, inject } from '@angular/core/testing';
import { HttpModule, XHRBackend, Response, ResponseOptions, RequestMethod } from '@angular/http';
import { MockBackend, MockConnection } from '@angular/http/testing';

import { UserService } from './user.service';

describe('UserService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpModule],
      providers: [
        UserService,
        { provide: XHRBackend, useClass: MockBackend }
      ]
    });
  });

  it('should be created', inject([UserService], (service: UserService) => {
    expect(service).toBeTruthy();
  }));

  it('getUsers() should GET http://localhost:8080/api/user/all',
    inject([UserService, XHRBackend], (service: UserService, backend: MockBackend) => {
      backend.connections.subscribe((conn: MockConnection) => {
        expect(conn.request.url).toBe('http://localhost:8080/api/user/all');
        expect(conn.request.method).toBe(RequestMethod.Get);
        conn.mockRespond(new Response(new ResponseOptions({ body: '[]' })));
      });

      service.getUsers().subscribe();
    }));

  it('enableUser() should GET http://localhost:8080/api/user/{username}/enable',
    inject([UserService, XHRBackend], (service: UserService, backend: MockBackend) => {
      backend.connections.subscribe((conn: MockConnection) => {
        expect(conn.request.url).toBe('http://localhost:8080/api/user/john/enable');
        expect(conn.request.method).toBe(RequestMethod.Get);
        conn.mockRespond(new Response(new ResponseOptions({ body: '' })));
      });

      service.enableUser('john').subscribe();
    }));

  it('disableUser() should GET http://localhost:8080/api/user/{username}/disable',
    inject([UserService, XHRBackend], (service: UserService, backend: MockBackend) => {
      backend.connections.subscribe((conn: MockConnection) => {
        expect(conn.request.url).toBe('http://localhost:8080/api/user/john/disable');
        expect(conn.request.method).toBe(RequestMethod.Get);
        conn.mockRespond(new Response(new ResponseOptions({ body: '' })));
      });

      service.disableUser('john').subscribe();
    }));

  it('getPrimaryTransactionList() should pass username as query param',
    inject([UserService, XHRBackend], (service: UserService, backend: MockBackend) => {
      backend.connections.subscribe((conn: MockConnection) => {
        expect(conn.request.url)
          .toBe('http://localhost:8080/api/user/primary/transaction?username=john');
        expect(conn.request.method).toBe(RequestMethod.Get);
        conn.mockRespond(new Response(new ResponseOptions({ body: '[]' })));
      });

      service.getPrimaryTransactionList('john').subscribe();
    }));

  it('getSavingsTransactionList() should pass username as query param',
    inject([UserService, XHRBackend], (service: UserService, backend: MockBackend) => {
      backend.connections.subscribe((conn: MockConnection) => {
        expect(conn.request.url)
          .toBe('http://localhost:8080/api/user/savings/transaction?username=john');
        expect(conn.request.method).toBe(RequestMethod.Get);
        conn.mockRespond(new Response(new ResponseOptions({ body: '[]' })));
      });

      service.getSavingsTransactionList('john').subscribe();
    }));
});
