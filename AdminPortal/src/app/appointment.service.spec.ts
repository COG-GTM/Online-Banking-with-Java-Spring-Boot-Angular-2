/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AppointmentService, Appointment } from './appointment.service';

describe('Service: Appointment', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AppointmentService]
    });
  });

  afterEach(inject([HttpTestingController], (httpMock: HttpTestingController) => {
    httpMock.verify();
  }));

  it('should ...', inject([AppointmentService], (service: AppointmentService) => {
    expect(service).toBeTruthy();
  }));

  it('should GET the appointment list with credentials', inject(
    [AppointmentService, HttpTestingController],
    (service: AppointmentService, httpMock: HttpTestingController) => {
      const mockList: Appointment[] = [
        { id: 1, date: '2017-01-01', location: 'Branch', description: 'Loan', confirmed: false, user: { username: 'bob' } }
      ];
      let result: Appointment[];
      service.getAppointmentList().subscribe(res => result = res);

      const req = httpMock.expectOne('http://localhost:8080/api/appointment/all');
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBe(true);
      req.flush(mockList);

      expect(result).toEqual(mockList);
    }));

  it('should GET the confirm endpoint for an appointment id', inject(
    [AppointmentService, HttpTestingController],
    (service: AppointmentService, httpMock: HttpTestingController) => {
      service.confirmAppointment(7).subscribe();

      const req = httpMock.expectOne('http://localhost:8080/api/appointment/7/confirm');
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBe(true);
      req.flush({});
    }));
});
