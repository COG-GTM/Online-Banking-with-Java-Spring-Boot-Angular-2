import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

export interface AppointmentUser {
  username: string;
}

export interface Appointment {
  id: number;
  date: string;
  location: string;
  description: string;
  confirmed: boolean;
  user: AppointmentUser;
}

@Injectable()
export class AppointmentService {

  constructor (private http: HttpClient) {}

  getAppointmentList(): Observable<Appointment[]> {
    let url = "http://localhost:8080/api/appointment/all";
    return this.http.get<Appointment[]>(url, { withCredentials: true });
  }

  confirmAppointment(id: number): Observable<Appointment> {
    let url = "http://localhost:8080/api/appointment/"+id+"/confirm";
    return this.http.get<Appointment>(url, { withCredentials: true });
  }

}
