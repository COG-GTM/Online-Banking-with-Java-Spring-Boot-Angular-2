/* tslint:disable:no-unused-variable */

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { LoginComponent } from './login.component';
import { LoginService } from '../login.service';

describe('Component: Login', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LoginComponent, LoginService, provideHttpClient(), provideHttpClientTesting()]
    });
  });

  it('should create an instance', () => {
    let component = TestBed.inject(LoginComponent);
    expect(component).toBeTruthy();
  });
});
