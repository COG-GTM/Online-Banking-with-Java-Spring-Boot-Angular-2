import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { AppointmentComponent } from './appointment.component';
import { AppointmentService } from '../appointment.service';

describe('Component: Appointment', () => {
  let component: AppointmentComponent;
  let fixture: ComponentFixture<AppointmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AppointmentComponent ],
      providers: [ AppointmentService, provideHttpClient(), provideHttpClientTesting(), provideRouter([]) ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AppointmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
