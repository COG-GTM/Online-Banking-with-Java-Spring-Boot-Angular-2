import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { LOGGED_IN_KEY, LoginService } from './login.service';

describe('LoginService', () => {
  let service: LoginService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(LoginService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('POSTs form-encoded credentials to /index with credentials', () => {
    let body: string | undefined;
    service.sendCredential('admin', 'p&ss word').subscribe((res) => (body = res));

    const req = httpMock.expectOne('http://localhost:8080/index');
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBe(true);
    expect(req.request.responseType).toBe('text');
    expect(req.request.headers.get('Content-Type')).toBe('application/x-www-form-urlencoded');
    expect(req.request.body).toBe('username=admin&password=p%26ss+word');
    req.flush('<html></html>');
    expect(body).toBe('<html></html>');
  });

  it('GETs /logout with credentials', () => {
    service.logout().subscribe();

    const req = httpMock.expectOne('http://localhost:8080/logout');
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush('');
  });

  it('tracks the logged-in flag in localStorage', () => {
    expect(service.loggedIn()).toBe(false);
    service.markLoggedIn();
    expect(service.loggedIn()).toBe(true);
    expect(localStorage.getItem(LOGGED_IN_KEY)).toBe('true');
    service.markLoggedOut();
    expect(service.loggedIn()).toBe(false);
    expect(localStorage.getItem(LOGGED_IN_KEY)).toBe('');
  });
});
