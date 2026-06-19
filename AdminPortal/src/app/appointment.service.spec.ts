import { TestBed, inject } from '@angular/core/testing';
import { HttpModule, XHRBackend, Response, ResponseOptions, RequestMethod } from '@angular/http';
import { MockBackend, MockConnection } from '@angular/http/testing';

import { AppointmentService } from './appointment.service';

describe('AppointmentService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpModule],
      providers: [
        AppointmentService,
        { provide: XHRBackend, useClass: MockBackend }
      ]
    });
  });

  it('should be created', inject([AppointmentService], (service: AppointmentService) => {
    expect(service).toBeTruthy();
  }));

  it('getAppointmentList() should GET /api/appointment/all',
    inject([AppointmentService, XHRBackend], (service: AppointmentService, backend: MockBackend) => {
      backend.connections.subscribe((conn: MockConnection) => {
        expect(conn.request.url).toBe('http://localhost:8080/api/appointment/all');
        expect(conn.request.method).toBe(RequestMethod.Get);
        conn.mockRespond(new Response(new ResponseOptions({ body: '[]' })));
      });

      service.getAppointmentList().subscribe();
    }));

  it('confirmAppointment() should GET /api/appointment/{id}/confirm',
    inject([AppointmentService, XHRBackend], (service: AppointmentService, backend: MockBackend) => {
      backend.connections.subscribe((conn: MockConnection) => {
        expect(conn.request.url).toBe('http://localhost:8080/api/appointment/7/confirm');
        expect(conn.request.method).toBe(RequestMethod.Get);
        conn.mockRespond(new Response(new ResponseOptions({ body: '' })));
      });

      service.confirmAppointment(7).subscribe();
    }));
});
