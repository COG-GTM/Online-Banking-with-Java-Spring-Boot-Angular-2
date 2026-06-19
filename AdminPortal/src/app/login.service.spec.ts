import { TestBed, inject } from '@angular/core/testing';
import { HttpModule, XHRBackend, Response, ResponseOptions, RequestMethod } from '@angular/http';
import { MockBackend, MockConnection } from '@angular/http/testing';

import { LoginService } from './login.service';

describe('LoginService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpModule],
      providers: [
        LoginService,
        { provide: XHRBackend, useClass: MockBackend }
      ]
    });
  });

  it('should be created', inject([LoginService], (service: LoginService) => {
    expect(service).toBeTruthy();
  }));

  it('sendCredential() should POST to /index with form-urlencoded body',
    inject([LoginService, XHRBackend], (service: LoginService, backend: MockBackend) => {
      backend.connections.subscribe((conn: MockConnection) => {
        expect(conn.request.url).toBe('http://localhost:8080/index');
        expect(conn.request.method).toBe(RequestMethod.Post);
        expect(conn.request.text()).toBe('username=admin&password=secret');
        expect(conn.request.headers.get('Content-Type')).toBe('application/x-www-form-urlencoded');
        conn.mockRespond(new Response(new ResponseOptions({ body: '' })));
      });

      service.sendCredential('admin', 'secret').subscribe();
    }));

  it('logout() should GET /logout',
    inject([LoginService, XHRBackend], (service: LoginService, backend: MockBackend) => {
      backend.connections.subscribe((conn: MockConnection) => {
        expect(conn.request.url).toBe('http://localhost:8080/logout');
        expect(conn.request.method).toBe(RequestMethod.Get);
        conn.mockRespond(new Response(new ResponseOptions({ body: '' })));
      });

      service.logout().subscribe();
    }));
});
