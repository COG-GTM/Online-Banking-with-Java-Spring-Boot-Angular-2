import { TestBed, inject } from '@angular/core/testing';
import { BaseRequestOptions, Http, Request, RequestMethod, RequestOptions, Response, ResponseOptions } from '@angular/http';
import { MockBackend, MockConnection } from '@angular/http/testing';
import { LoginService } from './login.service';
import { xsrfStrategyFactory } from './app.module';

describe('Service: Login', () => {
  let backend: MockBackend;
  let connections: MockConnection[];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        LoginService,
        MockBackend,
        BaseRequestOptions,
        {
          provide: Http,
          useFactory: (b: MockBackend, options: BaseRequestOptions) => new Http(b, options),
          deps: [MockBackend, BaseRequestOptions]
        }
      ]
    });
    backend = TestBed.get(MockBackend);
    connections = [];
    backend.connections.subscribe((c: MockConnection) => connections.push(c));
  });

  it('fetches the login page for the CSRF cookie before posting credentials', inject([LoginService], (service: LoginService) => {
    let done = false;
    service.sendCredential('admin', 'password').subscribe(() => done = true);

    expect(connections.length).toBe(1);
    expect(connections[0].request.method).toBe(RequestMethod.Get);
    expect(connections[0].request.url).toBe('http://localhost:8080/index');
    expect(connections[0].request.withCredentials).toBe(true);

    connections[0].mockRespond(new Response(new ResponseOptions({ status: 200 })));

    expect(connections.length).toBe(2);
    const post = connections[1].request;
    expect(post.method).toBe(RequestMethod.Post);
    expect(post.url).toBe('http://localhost:8080/index');
    expect(post.withCredentials).toBe(true);
    expect(post.getBody()).toBe('username=admin&password=password');
    expect(post.headers.get('Content-Type')).toBe('application/x-www-form-urlencoded');

    connections[1].mockRespond(new Response(new ResponseOptions({ status: 200 })));
    expect(done).toBe(true);
  }));

  it('does not post credentials when the login page request fails', inject([LoginService], (service: LoginService) => {
    let error: any;
    service.sendCredential('admin', 'password').subscribe(() => fail('should not succeed'), err => error = err);

    connections[0].mockError(new Error('network down'));

    expect(connections.length).toBe(1);
    expect(error).toBeDefined();
  }));
});

describe('xsrfStrategyFactory', () => {
  afterEach(() => {
    document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/';
  });

  it('copies the XSRF-TOKEN cookie into the X-XSRF-TOKEN header', () => {
    document.cookie = 'XSRF-TOKEN=test-token; path=/';
    const req = new Request(new RequestOptions({ method: RequestMethod.Post, url: 'http://localhost:8080/api/user/u/enable' }));

    xsrfStrategyFactory().configureRequest(req);

    expect(req.headers.get('X-XSRF-TOKEN')).toBe('test-token');
  });
});
