import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { AppointmentComponent } from './appointment.component';
import { AppointmentService } from '../appointment.service';

describe('AppointmentComponent', () => {
  let component: AppointmentComponent;
  let fixture: ComponentFixture<AppointmentComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AppointmentComponent],
      imports: [HttpClientTestingModule],
      providers: [AppointmentService]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(AppointmentComponent);
    component = fixture.componentInstance;
  });

  it('should be created', () => {
    httpMock.expectOne('http://localhost:8080/api/appointment/all').flush([]);
    expect(component).toBeTruthy();
  });

  it('should expose the appointments returned by the service', () => {
    const appointments = [{ id: 1, date: '2024-01-01' }];
    httpMock.expectOne('http://localhost:8080/api/appointment/all').flush(appointments);

    expect(component.appointmentList).toEqual(appointments);
  });
});
