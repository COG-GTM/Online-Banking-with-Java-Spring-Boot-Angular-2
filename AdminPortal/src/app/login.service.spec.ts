/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { BaseRequestOptions, Http, HttpModule, Request, RequestMethod, Response, ResponseOptions } from '@angular/http';
import { MockBackend, MockConnection } from '@angular/http/testing';
import { LoginService } from './login.service';

describe('Service: Login', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpModule],
      providers: [LoginService]
    });
  });

  it('should ...', inject([LoginService], (service: LoginService) => {
    expect(service).toBeTruthy();
  }));
});

describe('LoginService.sendCredential', () => {
  let backend: MockBackend;
  let service: LoginService;

  beforeEach(() => {
    backend = new MockBackend();
    service = new LoginService(new Http(backend, new BaseRequestOptions()));
  });

  it('bootstraps the XSRF cookie with a credentialed GET /index before POST /index', (done) => {
    const requests: Request[] = [];
    backend.connections.subscribe((c: MockConnection) => {
      requests.push(c.request);
      c.mockRespond(new Response(new ResponseOptions({ status: 200, body: '' })));
    });

    service.sendCredential('admin', 'secret').subscribe(() => {
      expect(requests.length).toBe(2);
      expect(requests[0].method).toBe(RequestMethod.Get);
      expect(requests[0].url).toBe('http://localhost:8080/index');
      expect(requests[0].withCredentials).toBe(true);
      expect(requests[1].method).toBe(RequestMethod.Post);
      expect(requests[1].url).toBe('http://localhost:8080/index');
      expect(requests[1].withCredentials).toBe(true);
      expect(requests[1].headers.get('Content-Type')).toBe('application/x-www-form-urlencoded');
      expect(requests[1].getBody()).toBe('username=admin&password=secret');
      done();
    });
  });

  it('still sends the login POST if the bootstrap GET fails', (done) => {
    const methods: RequestMethod[] = [];
    backend.connections.subscribe((c: MockConnection) => {
      methods.push(c.request.method);
      if (c.request.method === RequestMethod.Get) {
        c.mockError(new Error('network'));
      } else {
        c.mockRespond(new Response(new ResponseOptions({ status: 200, body: '' })));
      }
    });

    service.sendCredential('admin', 'secret').subscribe(() => {
      expect(methods).toEqual([RequestMethod.Get, RequestMethod.Post]);
      done();
    });
  });
});
