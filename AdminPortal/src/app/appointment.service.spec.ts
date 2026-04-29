/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AppointmentService } from './appointment.service';

describe('Service: Appointment', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AppointmentService]
    });
  });

  it('should ...', inject([AppointmentService], (service: AppointmentService) => {
    expect(service).toBeTruthy();
  }));
});
