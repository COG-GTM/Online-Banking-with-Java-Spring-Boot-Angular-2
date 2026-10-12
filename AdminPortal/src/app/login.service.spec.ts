import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { LoginService } from './login.service';

describe('LoginService', () => {
  let service: LoginService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LoginService]
    });
    service = TestBed.inject(LoginService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should post form-encoded credentials with credentials enabled', () => {
    service.sendCredential('admin', 'secret').subscribe();

    const req = httpMock.expectOne('http://localhost:8080/index');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBe('username=admin&password=secret');
    expect(req.request.headers.get('Content-Type')).toBe('application/x-www-form-urlencoded');
    expect(req.request.withCredentials).toBe(true);
    req.flush('');
  });

  it('should call the logout endpoint', () => {
    service.logout().subscribe();

    const req = httpMock.expectOne('http://localhost:8080/logout');
    expect(req.request.method).toBe('GET');
    req.flush('');
  });
});
