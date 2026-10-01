/* tslint:disable:no-unused-variable */

import { TestBed, inject } from '@angular/core/testing';
import { BaseRequestOptions, Http, Response, ResponseOptions } from '@angular/http';
import { MockBackend, MockConnection } from '@angular/http/testing';
import { UserService, LIST_CACHE_TTL_MS } from './user.service';

describe('Service: User', () => {
  let backend: MockBackend;
  let service: UserService;
  let requests: string[];
  let failNext: boolean;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        UserService,
        MockBackend,
        BaseRequestOptions,
        {
          provide: Http,
          useFactory: (mockBackend: MockBackend, options: BaseRequestOptions) => new Http(mockBackend, options),
          deps: [MockBackend, BaseRequestOptions]
        }
      ]
    });
  });

  beforeEach(inject([UserService, MockBackend], (userService: UserService, mockBackend: MockBackend) => {
    service = userService;
    backend = mockBackend;
    requests = [];
    failNext = false;
    backend.connections.subscribe((connection: MockConnection) => {
      requests.push(connection.request.url);
      if (failNext) {
        failNext = false;
        connection.mockError(new Error('boom'));
      } else {
        connection.mockRespond(new Response(new ResponseOptions({ body: '[]', status: 200 })));
      }
    });
  }));

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  it('should ...', () => {
    expect(service).toBeTruthy();
  });

  it('reuses the cached user list on repeat calls', () => {
    service.getUsers().subscribe();
    service.getUsers().subscribe();
    expect(requests.length).toBe(1);
  });

  it('caches transaction lists per username', () => {
    service.getPrimaryTransactionList('alice').subscribe();
    service.getPrimaryTransactionList('alice').subscribe();
    service.getPrimaryTransactionList('bob').subscribe();
    service.getSavingsTransactionList('alice').subscribe();
    service.getSavingsTransactionList('alice').subscribe();
    expect(requests.length).toBe(3);
  });

  it('refetches after the TTL expires', () => {
    jasmine.clock().install();
    jasmine.clock().mockDate(new Date(2017, 0, 1));
    service.getUsers().subscribe();
    jasmine.clock().tick(LIST_CACHE_TTL_MS + 1);
    service.getUsers().subscribe();
    expect(requests.length).toBe(2);
  });

  it('invalidates the user list after enable/disable', () => {
    service.getUsers().subscribe();
    service.enableUser('alice').subscribe();
    service.getUsers().subscribe();
    service.disableUser('alice').subscribe();
    service.getUsers().subscribe();
    expect(requests).toEqual([
      'http://localhost:8080/api/user/all',
      'http://localhost:8080/api/user/alice/enable',
      'http://localhost:8080/api/user/all',
      'http://localhost:8080/api/user/alice/disable',
      'http://localhost:8080/api/user/all'
    ]);
  });

  it('does not cache failed responses', () => {
    failNext = true;
    service.getUsers().subscribe(null, () => {});
    service.getUsers().subscribe();
    service.getUsers().subscribe();
    expect(requests.length).toBe(2);
  });
});
