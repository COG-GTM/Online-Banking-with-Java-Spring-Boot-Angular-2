/* tslint:disable:no-unused-variable */

import { TestBed, inject } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { LoginService } from './login.service';

describe('Service: Login', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LoginService, provideHttpClient(), provideHttpClientTesting()]
    });
  });

  it('should ...', inject([LoginService], (service: LoginService) => {
    expect(service).toBeTruthy();
  }));
});
