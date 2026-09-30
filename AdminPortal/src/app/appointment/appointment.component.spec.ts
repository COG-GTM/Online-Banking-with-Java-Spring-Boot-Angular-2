/* tslint:disable:no-unused-variable */

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AppointmentComponent } from './appointment.component';
import { AppointmentService } from '../appointment.service';

describe('Component: Appointment', () => {
  it('should create an instance', () => {
    TestBed.configureTestingModule({
      declarations: [AppointmentComponent],
      imports: [FormsModule],
      providers: [AppointmentService, provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
    const component = TestBed.createComponent(AppointmentComponent).componentInstance;
    expect(component).toBeTruthy();
  });
});
