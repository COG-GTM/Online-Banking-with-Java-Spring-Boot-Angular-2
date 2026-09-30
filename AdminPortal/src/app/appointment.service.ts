import { Injectable } from '@angular/core';
import {Http, Headers} from '@angular/http';


@Injectable()
export class AppointmentService {

  constructor (private http:Http){}

  getAppointmentList() {
    let url = "http://localhost:8080/api/appointment/all";
    return this.http.get(url, { withCredentials: true });
  }

  confirmAppointment(id: number) {
    let url = "http://localhost:8080/api/appointment/"+id+"/confirm";
    let headers = new Headers();
    let xsrfToken = this.readCookie('XSRF-TOKEN');
    if (xsrfToken) {
      headers.append('X-XSRF-TOKEN', xsrfToken);
    }
    return this.http.post(url, null, { headers: headers, withCredentials: true });
  }

  private readCookie(name: string): string {
    let match = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'));
    return match ? decodeURIComponent(match[1]) : null;
  }

}
