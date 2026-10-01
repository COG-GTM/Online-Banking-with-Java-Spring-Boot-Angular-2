import { Injectable } from '@angular/core';
import {Http, Headers} from '@angular/http';


@Injectable()
export class AppointmentService {

  constructor (private http:Http){}

  getAppointmentList(page: number, size: number) {
    let url = "http://localhost:8080/api/appointment/all?page="+page+"&size="+size;
    return this.http.get(url, { withCredentials: true });
  }

  confirmAppointment(id: number) {
    let url = "http://localhost:8080/api/appointment/"+id+"/confirm";
    return this.http.get(url, { withCredentials: true });
  }

}
