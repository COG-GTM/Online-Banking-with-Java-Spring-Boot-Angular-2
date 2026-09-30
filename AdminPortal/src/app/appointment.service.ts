import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Appointment } from './models';

@Injectable({ providedIn: 'root' })
export class AppointmentService {
  private readonly http = inject(HttpClient);

  getAppointmentList(): Observable<Appointment[]> {
    const url = 'http://localhost:8080/api/appointment/all';
    return this.http.get<Appointment[]>(url, { withCredentials: true });
  }

  confirmAppointment(id: number): Observable<unknown> {
    const url = 'http://localhost:8080/api/appointment/' + id + '/confirm';
    return this.http.get(url, { withCredentials: true });
  }
}
