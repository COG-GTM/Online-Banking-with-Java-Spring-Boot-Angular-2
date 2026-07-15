import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { environment } from '../environments/environment';


@Injectable()
export class AppointmentService {

  private baseUrl = environment.apiUrl;

  constructor (private http: HttpClient) {}

  getAppointmentList(): Observable<any> {
    let url = this.baseUrl + '/api/appointment/all';
    return this.http.get(url, { withCredentials: true });
  }

  confirmAppointment(id: number): Observable<any> {
    let url = this.baseUrl + '/api/appointment/' + id + '/confirm';
    return this.http.get(url, { withCredentials: true });
  }

}
