/* tslint:disable:no-unused-variable */

import { TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule, XHRBackend } from '@angular/http';
import { MockBackend } from '@angular/http/testing';

import { LoginComponent } from './login.component';
import { LoginService } from '../login.service';

describe('Component: Login', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [FormsModule, HttpModule],
      declarations: [LoginComponent],
      providers: [
        LoginService,
        { provide: XHRBackend, useClass: MockBackend }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    });
  });

  it('should create an instance', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });
});
