/* tslint:disable:no-unused-variable */

import { TestBed, async } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AppointmentComponent } from './appointment.component';
import { AppointmentService } from '../appointment.service';

describe('Component: Appointment', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [AppointmentComponent],
      providers: [AppointmentService]
    }).compileComponents();
  }));

  it('should create an instance', () => {
    const fixture = TestBed.createComponent(AppointmentComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
