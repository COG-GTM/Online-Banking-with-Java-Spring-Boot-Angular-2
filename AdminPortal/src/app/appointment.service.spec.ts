import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { AppointmentService } from './appointment.service';

describe('AppointmentService', () => {
  let service: AppointmentService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AppointmentService]
    });
    service = TestBed.inject(AppointmentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should request the appointment list', () => {
    const appointments = [{ id: 1 }];
    let result: object[] | undefined;

    service.getAppointmentList().subscribe(res => (result = res));

    const req = httpMock.expectOne('http://localhost:8080/api/appointment/all');
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush(appointments);

    expect(result).toEqual(appointments);
  });

  it('should confirm an appointment', () => {
    service.confirmAppointment(7).subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/appointment/7/confirm');
    expect(req.request.method).toBe('GET');
    req.flush('');
  });
});
