import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { AppointmentService } from './appointment.service';
import { Appointment } from './models';

describe('AppointmentService', () => {
  let service: AppointmentService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AppointmentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('GETs /api/appointment/all with credentials', () => {
    const appointments = [{ id: 1 }] as Appointment[];
    let result: Appointment[] | undefined;
    service.getAppointmentList().subscribe((res) => (result = res));

    const req = httpMock.expectOne('http://localhost:8080/api/appointment/all');
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush(appointments);
    expect(result).toEqual(appointments);
  });

  it('GETs /api/appointment/{id}/confirm with credentials', () => {
    service.confirmAppointment(7).subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/appointment/7/confirm');
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush(null);
  });
});
