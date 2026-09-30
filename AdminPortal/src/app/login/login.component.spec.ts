/* tslint:disable:no-unused-variable */

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { LoginComponent } from './login.component';
import { LoginService } from '../login.service';

describe('Component: Login', () => {
  it('should create an instance', () => {
    TestBed.configureTestingModule({
      declarations: [LoginComponent],
      providers: [LoginService, provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
    let component = TestBed.createComponent(LoginComponent).componentInstance;
    expect(component).toBeTruthy();
  });
});
