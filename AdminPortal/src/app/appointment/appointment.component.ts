import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { AppointmentService } from '../appointment.service';
import { Appointment } from '../models';

@Component({
  selector: 'app-appointment',
  imports: [DatePipe],
  templateUrl: './appointment.component.html',
  styleUrl: './appointment.component.css',
})
export class AppointmentComponent {
  private readonly appointmentService = inject(AppointmentService);

  protected readonly appointmentList = signal<Appointment[]>([]);

  constructor() {
    this.getAppointmentList();
  }

  getAppointmentList(): void {
    this.appointmentService.getAppointmentList().subscribe({
      next: (appointments) => this.appointmentList.set(appointments),
      error: (err) => console.log(err),
    });
  }

  confirmAppointment(id: number): void {
    this.appointmentService.confirmAppointment(id).subscribe({
      next: () => this.getAppointmentList(),
      error: (err) => console.log(err),
    });
  }
}
