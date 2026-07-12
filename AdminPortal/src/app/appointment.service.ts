import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../environments/environment';
import { Appointment } from './models';

@Injectable({ providedIn: 'root' })
export class AppointmentService {

  constructor(private http: HttpClient) {}

  getAppointmentList(): Observable<Appointment[]> {
    const url = `${environment.apiBaseUrl}/api/appointment/all`;
    return this.http.get<Appointment[]>(url, { withCredentials: true });
  }

  confirmAppointment(id: number): Observable<unknown> {
    const url = `${environment.apiBaseUrl}/api/appointment/${id}/confirm`;
    return this.http.get(url, { withCredentials: true });
  }
}
