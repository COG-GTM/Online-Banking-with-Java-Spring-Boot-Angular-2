/* tslint:disable:no-unused-variable */

import { TestBed, inject } from '@angular/core/testing';
import { HttpModule, XHRBackend, RequestMethod, ResponseOptions, Response } from '@angular/http';
import { MockBackend, MockConnection } from '@angular/http/testing';

import { AppointmentService } from './appointment.service';

describe('Service: Appointment', () => {
  let backend: MockBackend;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpModule],
      providers: [
        AppointmentService,
        { provide: XHRBackend, useClass: MockBackend }
      ]
    });
  });

  beforeEach(inject([XHRBackend], (mockBackend: MockBackend) => {
    backend = mockBackend;
  }));

  function expectGet(url: string) {
    backend.connections.subscribe((conn: MockConnection) => {
      expect(conn.request.method).toBe(RequestMethod.Get);
      expect(conn.request.url).toBe(url);
      expect(conn.request.withCredentials).toBe(true);
      conn.mockRespond(new Response(new ResponseOptions({ body: '[]' })));
    });
  }

  it('should be created', inject([AppointmentService], (service: AppointmentService) => {
    expect(service).toBeTruthy();
  }));

  it('getAppointmentList should GET /api/appointment/all with credentials',
    inject([AppointmentService], (service: AppointmentService) => {
      expectGet('http://localhost:8080/api/appointment/all');
      service.getAppointmentList().subscribe();
  }));

  it('confirmAppointment should GET /api/appointment/{id}/confirm with credentials',
    inject([AppointmentService], (service: AppointmentService) => {
      expectGet('http://localhost:8080/api/appointment/42/confirm');
      service.confirmAppointment(42).subscribe();
  }));
});
