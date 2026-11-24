import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';

import { AppointmentComponent } from './appointment.component';
import { AppointmentService } from '../appointment.service';

describe('AppointmentComponent', () => {
  let fixture: ComponentFixture<AppointmentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [AppointmentComponent],
      providers: [{ provide: AppointmentService, useValue: { getAppointmentList: () => Observable.of({ _body: JSON.stringify([{ id: 1, user: { username: "alice" }, date: 0, description: "Loan", confirmed: false }]) }) } }],
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
