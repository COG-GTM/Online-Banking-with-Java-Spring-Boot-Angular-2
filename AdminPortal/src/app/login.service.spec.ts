/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { LoginService } from './login.service';

describe('Service: Login', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LoginService]
    });
  });

  afterEach(inject([HttpTestingController], (httpMock: HttpTestingController) => {
    httpMock.verify();
  }));

  it('should ...', inject([LoginService], (service: LoginService) => {
    expect(service).toBeTruthy();
  }));

  it('should send credentials', inject([LoginService, HttpTestingController],
    (service: LoginService, httpMock: HttpTestingController) => {
      service.sendCredential('u', 'p').subscribe();

      const req = httpMock.expectOne('http://localhost:8080/index');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toBe('username=u&password=p');
      expect(req.request.headers.get('Content-Type')).toBe('application/x-www-form-urlencoded');
      expect(req.request.withCredentials).toBe(true);
      expect(req.request.responseType).toBe('text');
      req.flush('ok');
    }));

  it('should log out', inject([LoginService, HttpTestingController],
    (service: LoginService, httpMock: HttpTestingController) => {
      service.logout().subscribe();

      const req = httpMock.expectOne('http://localhost:8080/logout');
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBe(true);
      expect(req.request.responseType).toBe('text');
      req.flush('ok');
    }));
});
