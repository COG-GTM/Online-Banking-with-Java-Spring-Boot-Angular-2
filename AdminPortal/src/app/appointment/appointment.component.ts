import { Component, OnInit, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {AppointmentService} from '../appointment.service';
import { DatePipe } from '@angular/common';


@Component({
    selector: 'app-appointment',
    templateUrl: './appointment.component.html',
    styleUrls: ['./appointment.component.css'],
    imports: [DatePipe]
})
export class AppointmentComponent implements OnInit {
  private appointmentService = inject(AppointmentService);
  appointmentList = toSignal(
    this.appointmentService.getAppointmentList().pipe(
      map(res => res as any[]),
      catchError(error => { console.log(error); return of([]); })
    ),
    { initialValue: [] as any[] }
  );

	confirmAppointment(id: number) {
  		this.appointmentService.confirmAppointment(id).subscribe();
  		location.reload();
  	}

ngOnInit() {}
}
