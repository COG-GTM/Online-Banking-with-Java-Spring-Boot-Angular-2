/* tslint:disable:no-unused-variable */

import { TestBed, async, ComponentFixture } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AppointmentComponent } from './appointment.component';
import { AppointmentService } from '../appointment.service';

describe('Component: Appointment', () => {
  let fixture: ComponentFixture<AppointmentComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [AppointmentComponent],
      providers: [AppointmentService]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppointmentComponent);
    httpMock = TestBed.get(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create an instance', () => {
    expect(fixture.componentInstance).toBeTruthy();
    httpMock.expectOne('http://localhost:8080/api/appointment/all').flush([]);
  });

  it('should populate appointmentList from the parsed response', () => {
    const appointments = [
      { id: 1, date: '2017-01-01', location: 'Branch', description: 'Loan', confirmed: false, user: { username: 'bob' } }
    ];
    httpMock.expectOne('http://localhost:8080/api/appointment/all').flush(appointments);
    expect(fixture.componentInstance.appointmentList).toEqual(appointments);
  });
});
