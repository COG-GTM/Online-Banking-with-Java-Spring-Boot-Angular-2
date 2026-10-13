import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { Appointment } from './models';

@Injectable({ providedIn: 'root' })
export class AppointmentService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/appointment`;

  getAppointmentList(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.baseUrl}/all`, { withCredentials: true });
  }

  confirmAppointment(id: number): Observable<string> {
    return this.http.get(`${this.baseUrl}/${id}/confirm`, {
      withCredentials: true,
      responseType: 'text',
    });
  }
}
