/* tslint:disable:no-unused-variable */

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { LoginService } from '../login.service';
import { LoginComponent } from './login.component';

describe('Component: Login', () => {
  it('should create an instance', () => {
    TestBed.configureTestingModule({
      providers: [LoginComponent, LoginService, provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
    let component = TestBed.inject(LoginComponent);
    expect(component).toBeTruthy();
  });
});
