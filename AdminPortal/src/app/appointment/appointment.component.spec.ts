import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { AppointmentComponent } from './appointment.component';
import { AppointmentService } from '../appointment.service';

describe('AppointmentComponent', () => {
  let fixture: ComponentFixture<AppointmentComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [AppointmentComponent],
      providers: [{ provide: AppointmentService, useValue: { getAppointmentList: () => of([{ id: 1, user: { username: "alice" }, date: 0, description: "Loan", confirmed: false }]) } }],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  }));

  it('should render one row per item', () => {
    fixture = TestBed.createComponent(AppointmentComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(1);
    expect(fixture.nativeElement.textContent).toContain('Loan');
  });
});
