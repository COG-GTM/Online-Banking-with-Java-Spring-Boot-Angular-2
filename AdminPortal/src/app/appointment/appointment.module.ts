import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpModule } from '@angular/http';
import { AppointmentRoutingModule } from './appointment-routing.module';
import { AppointmentComponent } from './appointment.component';
import { AppointmentService } from './appointment.service';

@NgModule({
  declarations: [AppointmentComponent],
  imports: [CommonModule, HttpModule, AppointmentRoutingModule],
  providers: [AppointmentService]
})
export class AppointmentModule { }
