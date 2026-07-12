import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { AppointmentService } from './appointment.service';
import { environment } from '../environments/environment';

describe('AppointmentService', () => {
  let service: AppointmentService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AppointmentService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AppointmentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('fetches the appointment list', () => {
    service.getAppointmentList().subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/api/appointment/all`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('confirms an appointment by id', () => {
    service.confirmAppointment(7).subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/api/appointment/7/confirm`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });
});
