import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';

import { AppointmentComponent } from './appointment.component';
import { AppointmentService } from '../appointment.service';

const APPOINTMENTS = [
  {
    id: 1,
    user: { username: 'john' },
    date: '2020-01-01T10:00:00',
    description: 'Loan consultation',
    confirmed: false
  },
  {
    id: 2,
    user: { username: 'jane' },
    date: '2020-02-02T11:00:00',
    description: 'Account review',
    confirmed: true
  }
];

class MockAppointmentService {
  getAppointmentList() {
    return Observable.of({ _body: JSON.stringify(APPOINTMENTS) });
  }
  confirmAppointment(id: number) {
    return Observable.of({});
  }
}

describe('AppointmentComponent', () => {
  let component: AppointmentComponent;
  let fixture: ComponentFixture<AppointmentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [CommonModule],
      declarations: [AppointmentComponent],
      providers: [
        { provide: AppointmentService, useClass: MockAppointmentService }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppointmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should load the appointment list from the service', () => {
    expect(component.appointmentList.length).toBe(2);
  });

  it('should render a table row per appointment', () => {
    const rows = fixture.debugElement.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(2);
    expect(fixture.debugElement.nativeElement.textContent).toContain('Loan consultation');
    expect(fixture.debugElement.nativeElement.textContent).toContain('Account review');
  });
});
