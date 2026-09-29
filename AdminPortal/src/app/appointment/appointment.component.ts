import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';

import { Appointment } from '../models';
import { AppointmentService } from '../appointment.service';

@Component({
  selector: 'app-appointment',
  imports: [DatePipe],
  templateUrl: './appointment.component.html',
  styleUrl: './appointment.component.css',
})
export class AppointmentComponent {
  private readonly appointmentService = inject(AppointmentService);

  readonly appointmentList = signal<Appointment[]>([]);

  constructor() {
    this.getAppointmentList();
  }

  getAppointmentList(): void {
    this.appointmentService.getAppointmentList().subscribe({
      next: (appointments) => this.appointmentList.set(appointments),
      error: (error) => console.log(error),
    });
  }

  confirmAppointment(id: number): void {
    this.appointmentService.confirmAppointment(id).subscribe({
      complete: () => this.getAppointmentList(),
      error: (error) => console.log(error),
    });
  }
}
