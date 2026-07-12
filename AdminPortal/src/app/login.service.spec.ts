import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { LoginService } from './login.service';
import { environment } from '../environments/environment';

describe('LoginService', () => {
  let service: LoginService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LoginService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(LoginService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('posts encoded credentials in the body, not the URL', () => {
    service.sendCredential('al ice', 'p@ss&word').subscribe();

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/index`);
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBeTrue();
    expect(req.request.body).toBe('username=al%20ice&password=p%40ss%26word');
    expect(req.request.headers.get('Content-Type')).toBe('application/x-www-form-urlencoded');
    req.flush('ok');
  });

  it('logs out via GET /logout', () => {
    service.logout().subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/logout`);
    expect(req.request.method).toBe('GET');
    req.flush('ok');
  });
});
