/* tslint:disable:no-unused-variable */

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AppointmentComponent } from './appointment.component';

describe('Component: Appointment', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AppointmentComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
      schemas: [NO_ERRORS_SCHEMA]
    });
  });

  it('should create an instance', () => {
    let component = TestBed.createComponent(AppointmentComponent).componentInstance;
    expect(component).toBeTruthy();
  });
});
