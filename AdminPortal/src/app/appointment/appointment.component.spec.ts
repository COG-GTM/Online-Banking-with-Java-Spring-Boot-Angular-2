import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AppointmentService } from '../appointment.service';
import { AppointmentComponent } from './appointment.component';

describe('AppointmentComponent', () => {
  const appointmentService = {
    getAppointmentList: vi.fn(() =>
      of([
        {
          id: 3,
          date: '2017-06-20T10:30:00',
          location: 'Branch',
          description: 'Loan review',
          confirmed: false,
          user: { username: 'jdoe' },
        },
      ]),
    ),
    confirmAppointment: vi.fn(() => of('')),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      imports: [AppointmentComponent],
      providers: [{ provide: AppointmentService, useValue: appointmentService }],
    });
  });

  it('renders appointments with a confirm action', async () => {
    const fixture = TestBed.createComponent(AppointmentComponent);
    await fixture.whenStable();
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Loan review');
    expect(text).toContain('06/20/2017');
    expect(text).toContain('Confirm');
  });

  it('confirms and reloads the list', () => {
    const fixture = TestBed.createComponent(AppointmentComponent);
    fixture.componentInstance.confirmAppointment(3);
    expect(appointmentService.confirmAppointment).toHaveBeenCalledWith(3);
    expect(appointmentService.getAppointmentList).toHaveBeenCalledTimes(2);
  });
});
