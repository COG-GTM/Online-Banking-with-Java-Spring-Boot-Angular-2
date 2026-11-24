import { Component, OnInit } from '@angular/core';
import {AppointmentService} from '../appointment.service';
import { DatePipe } from '@angular/common';


@Component({
    selector: 'app-appointment',
    templateUrl: './appointment.component.html',
    styleUrls: ['./appointment.component.css'],
    imports: [DatePipe]
})
export class AppointmentComponent implements OnInit {

  appointmentList: Object[];

	constructor(private appointmentService: AppointmentService) {
		this.getAppointmentList();
	}

	getAppointmentList() {
		this.appointmentService.getAppointmentList().subscribe(
			res => {
        		this.appointmentList = res as Object[];
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
