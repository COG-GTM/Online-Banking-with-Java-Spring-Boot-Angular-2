/* tslint:disable:no-unused-variable */

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AppointmentComponent } from './appointment.component';
import { AppointmentService } from '../appointment.service';

describe('Component: Appointment', () => {
  it('should create an instance', () => {
    TestBed.configureTestingModule({
      declarations: [AppointmentComponent],
      providers: [AppointmentService, provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
    let component = TestBed.createComponent(AppointmentComponent).componentInstance;
    expect(component).toBeTruthy();
  });
});
