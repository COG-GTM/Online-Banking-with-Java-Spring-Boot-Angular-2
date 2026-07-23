/* tslint:disable:no-unused-variable */

import { TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { HttpModule, XHRBackend } from '@angular/http';
import { MockBackend } from '@angular/http/testing';

import { AppointmentComponent } from './appointment.component';
import { AppointmentService } from '../appointment.service';

describe('Component: Appointment', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpModule],
      declarations: [AppointmentComponent],
      providers: [
        AppointmentService,
        { provide: XHRBackend, useClass: MockBackend }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    });
  });

  it('should create an instance', () => {
    const fixture = TestBed.createComponent(AppointmentComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });
});
