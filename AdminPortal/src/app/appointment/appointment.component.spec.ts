/* tslint:disable:no-unused-variable */

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { AppointmentComponent } from './appointment.component';
import { AppointmentService } from '../appointment.service';

describe('Component: Appointment', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AppointmentComponent, AppointmentService, provideHttpClient(), provideHttpClientTesting()]
    });
  });

  it('should create an instance', () => {
    let component = TestBed.inject(AppointmentComponent);
    expect(component).toBeTruthy();
  });
});
