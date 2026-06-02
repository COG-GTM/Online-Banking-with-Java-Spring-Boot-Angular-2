import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../environments/environment';

@Injectable({ providedIn: 'root' })
export class AppointmentService {

  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getAppointmentList(): Observable<any[]> {
    const url = `${this.baseUrl}/api/appointment/all`;
    return this.http.get<any[]>(url, { withCredentials: true });
  }

  confirmAppointment(id: number): Observable<void> {
    const url = `${this.baseUrl}/api/appointment/${id}/confirm`;
    return this.http.get<void>(url, { withCredentials: true });
  }
}
