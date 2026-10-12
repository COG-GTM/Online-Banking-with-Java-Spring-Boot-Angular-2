import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { AppointmentService } from './appointment.service';

describe('Service: Appointment', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AppointmentService, provideHttpClient(), provideHttpClientTesting()]
    });
  });

  it('should ...', () => {
    expect(TestBed.inject(AppointmentService)).toBeTruthy();
  });
});
