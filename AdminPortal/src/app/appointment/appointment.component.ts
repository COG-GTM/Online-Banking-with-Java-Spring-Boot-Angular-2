import { Component, OnInit } from '@angular/core';
import { AppointmentService, Appointment } from '../appointment.service';


@Component({
  selector: 'app-appointment',
  templateUrl: './appointment.component.html',
  styleUrls: ['./appointment.component.css']
})
export class AppointmentComponent implements OnInit {

  appointmentList: Appointment[];

	constructor(private appointmentService: AppointmentService) {
		this.getAppointmentList();
	}

	getAppointmentList() {
		this.appointmentService.getAppointmentList().subscribe(
			(res: Appointment[]) => {
        		this.appointmentList = res;
      		},
      		error => console.log(error)
		)
	}	

	confirmAppointment(id: number) {
  		this.appointmentService.confirmAppointment(id).subscribe();
  		location.reload();
  	}

ngOnInit() {}
}
