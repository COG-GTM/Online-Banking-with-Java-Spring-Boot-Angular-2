import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AppointmentService } from '../appointment.service';
import { Appointment } from '../models';

@Component({
  selector: 'app-appointment',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './appointment.component.html',
  styleUrls: ['./appointment.component.css'],
})
export class AppointmentComponent implements OnInit {

  appointmentList: Appointment[] = [];

  constructor(private appointmentService: AppointmentService) {}

  ngOnInit(): void {
    this.getAppointmentList();
  }

  getAppointmentList(): void {
    this.appointmentService.getAppointmentList().subscribe({
      next: (appointments) => (this.appointmentList = appointments),
      error: (error) => console.log(error),
    });
  }

  confirmAppointment(id: number): void {
    this.appointmentService.confirmAppointment(id).subscribe({
      next: () => location.reload(),
      error: (error) => console.log(error),
    });
  }
}
