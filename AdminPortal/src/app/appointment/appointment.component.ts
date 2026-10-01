import { Component, OnInit } from '@angular/core';
import {AppointmentService} from '../appointment.service';


@Component({
  selector: 'app-appointment',
  templateUrl: './appointment.component.html',
  styleUrls: ['./appointment.component.css']
})
export class AppointmentComponent implements OnInit {

  appointmentList: Object[];
  page: number = 0;
  pageSize: number = 50;
  totalCount: number = 0;

	constructor(private appointmentService: AppointmentService) {
		this.getAppointmentList();
	}

	getAppointmentList() {
		this.appointmentService.getAppointmentList(this.page, this.pageSize).subscribe(
			res => {
        		this.appointmentList = JSON.parse(JSON.parse(JSON.stringify(res))._body);
        		this.totalCount = Number(res.headers.get('X-Total-Count')) || 0;
      		},
      		error => console.log(error)
		)
	}	

	pageCount() {
		return Math.max(1, Math.ceil(this.totalCount / this.pageSize));
	}

	previousPage() {
		if (this.page > 0) {
			this.page--;
			this.getAppointmentList();
		}
	}

	nextPage() {
		if (this.page + 1 < this.pageCount()) {
			this.page++;
			this.getAppointmentList();
		}
	}

	confirmAppointment(id: number) {
  		this.appointmentService.confirmAppointment(id).subscribe();
  		location.reload();
  	}

ngOnInit() {}
}
