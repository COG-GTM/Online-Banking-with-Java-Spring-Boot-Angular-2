import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AppointmentService } from '../appointment.service';

@Component({
  selector: 'app-appointment',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './appointment.component.html',
  styleUrls: ['./appointment.component.css']
})
export class AppointmentComponent {

  appointmentList: any[] = [];

  constructor(private appointmentService: AppointmentService) {
    this.getAppointmentList();
  }

  getAppointmentList(): void {
    this.appointmentService.getAppointmentList().subscribe({
      next: (res) => this.appointmentList = res,
      error: (error) => console.log(error)
    });
  }

  confirmAppointment(id: number): void {
    this.appointmentService.confirmAppointment(id).subscribe({
      complete: () => location.reload()
    });
  }
}
