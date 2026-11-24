import { TestBed, inject } from '@angular/core/testing';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { AppointmentService } from './appointment.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('AppointmentService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
    imports: [],
    providers: [AppointmentService, provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
});
  });

  it('should be created', inject([AppointmentService], (service: AppointmentService) => {
    expect(service).toBeTruthy();
  }));
});
