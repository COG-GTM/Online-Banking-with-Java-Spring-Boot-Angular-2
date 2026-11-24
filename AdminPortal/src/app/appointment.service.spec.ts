import { TestBed, inject } from '@angular/core/testing';
import { HttpModule } from '@angular/http';

import { AppointmentService } from './appointment.service';

describe('AppointmentService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpModule],
      providers: [AppointmentService]
    });
  });

  it('should be created', inject([AppointmentService], (service: AppointmentService) => {
    expect(service).toBeTruthy();
  }));
});
