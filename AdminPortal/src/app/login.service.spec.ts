/* tslint:disable:no-unused-variable */

import { TestBed, inject } from '@angular/core/testing';
import { HttpModule, XHRBackend, RequestMethod, ResponseOptions, Response } from '@angular/http';
import { MockBackend, MockConnection } from '@angular/http/testing';

import { LoginService } from './login.service';

describe('Service: Login', () => {
  let backend: MockBackend;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpModule],
      providers: [
        LoginService,
        { provide: XHRBackend, useClass: MockBackend }
      ]
    });
  });

  beforeEach(inject([XHRBackend], (mockBackend: MockBackend) => {
    backend = mockBackend;
  }));

  it('should be created', inject([LoginService], (service: LoginService) => {
    expect(service).toBeTruthy();
  }));

  it('sendCredential should POST a form-urlencoded body to /index with credentials',
    inject([LoginService], (service: LoginService) => {
      backend.connections.subscribe((conn: MockConnection) => {
        expect(conn.request.method).toBe(RequestMethod.Post);
        expect(conn.request.url).toBe('http://localhost:8080/index');
        expect(conn.request.withCredentials).toBe(true);
        expect(conn.request.headers.get('Content-Type')).toBe('application/x-www-form-urlencoded');
        expect(conn.request.getBody()).toBe('username=alice&password=secret');
        conn.mockRespond(new Response(new ResponseOptions({ body: '' })));
      });
      service.sendCredential('alice', 'secret').subscribe();
  }));

  it('logout should GET /logout with credentials',
    inject([LoginService], (service: LoginService) => {
      backend.connections.subscribe((conn: MockConnection) => {
        expect(conn.request.method).toBe(RequestMethod.Get);
        expect(conn.request.url).toBe('http://localhost:8080/logout');
        expect(conn.request.withCredentials).toBe(true);
        conn.mockRespond(new Response(new ResponseOptions({ body: '' })));
      });
      service.logout().subscribe();
  }));
});
