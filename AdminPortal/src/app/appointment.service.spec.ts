import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AppointmentService } from './appointment.service';

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

  it('lists appointments with cookies', () => {
    service.getAppointmentList().subscribe();
    const req = httpMock.expectOne('http://localhost:8080/api/appointment/all');
    expect(req.request.withCredentials).toBe(true);
    req.flush([]);
  });

  it('confirms an appointment', () => {
    service.confirmAppointment(7).subscribe();
    httpMock.expectOne('http://localhost:8080/api/appointment/7/confirm').flush('');
  });
});
