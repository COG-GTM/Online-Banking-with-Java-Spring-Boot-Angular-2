import { TestBed, inject } from '@angular/core/testing';
import { BaseRequestOptions, Http, RequestMethod } from '@angular/http';
import { MockBackend, MockConnection } from '@angular/http/testing';
import { AppointmentService } from './appointment.service';

describe('Service: Appointment', () => {
  let connections: MockConnection[];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AppointmentService,
        MockBackend,
        BaseRequestOptions,
        {
          provide: Http,
          useFactory: (b: MockBackend, options: BaseRequestOptions) => new Http(b, options),
          deps: [MockBackend, BaseRequestOptions]
        }
      ]
    });
    connections = [];
    TestBed.get(MockBackend).connections.subscribe((c: MockConnection) => connections.push(c));
  });

  function expectCredentialedPost(url: string) {
    expect(connections.length).toBe(1);
    expect(connections[0].request.method).toBe(RequestMethod.Post);
    expect(connections[0].request.url).toBe(url);
    expect(connections[0].request.withCredentials).toBe(true);
  }

  it('confirms an appointment with POST', inject([AppointmentService], (service: AppointmentService) => {
    service.confirmAppointment(9).subscribe();
    expectCredentialedPost('http://localhost:8080/api/appointment/9/confirm');
  }));
});
