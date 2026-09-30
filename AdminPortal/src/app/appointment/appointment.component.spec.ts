/* tslint:disable:no-unused-variable */

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AppointmentService } from '../appointment.service';
import { AppointmentComponent } from './appointment.component';

describe('Component: Appointment', () => {
  it('should create an instance', () => {
    TestBed.configureTestingModule({
      providers: [AppointmentComponent, AppointmentService, provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
    let component = TestBed.inject(AppointmentComponent);
    expect(component).toBeTruthy();
  });
});
