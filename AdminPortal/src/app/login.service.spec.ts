import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { LOGGED_IN_KEY, LoginService } from './login.service';

describe('LoginService', () => {
  let service: LoginService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(LoginService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => httpMock.verify());

  it('posts form-encoded credentials with cookies', () => {
    service.sendCredential('admin', 'p&ss').subscribe();
    const req = httpMock.expectOne('http://localhost:8080/index');
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBe(true);
    expect(req.request.headers.get('Content-Type')).toBe('application/x-www-form-urlencoded');
    expect(req.request.body).toBe('username=admin&password=p%26ss');
    req.flush('<html></html>');
  });

  it('calls logout with cookies', () => {
    service.logout().subscribe();
    const req = httpMock.expectOne('http://localhost:8080/logout');
    expect(req.request.withCredentials).toBe(true);
    req.flush('');
  });

  it('reads the logged-in flag from localStorage', () => {
    expect(service.isLoggedIn()).toBe(false);
    localStorage.setItem(LOGGED_IN_KEY, '');
    expect(service.isLoggedIn()).toBe(false);
    localStorage.setItem(LOGGED_IN_KEY, 'true');
    expect(service.isLoggedIn()).toBe(true);
  });
});
