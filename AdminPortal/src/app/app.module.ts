import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { routing }  from './app.routing';


import { AppComponent } from './app.component';
import { NavbarComponent } from './navbar/navbar.component';
import { LoginComponent } from './login/login.component';

import { LoginService } from './login.service';
import { UserAccountComponent } from './user-account/user-account.component';
import { UserService } from './user.service';
import { PrimaryTransactionComponent } from './primary-transaction/primary-transaction.component';
import { SavingsTransactionComponent } from './savings-transaction/savings-transaction.component';
import { AppointmentComponent } from './appointment/appointment.component';
import { AppointmentService } from './appointment.service';



@NgModule({ declarations: [
        AppComponent,
        NavbarComponent,
        LoginComponent,
        UserAccountComponent,
        PrimaryTransactionComponent,
        SavingsTransactionComponent,
        AppointmentComponent
    ],
    bootstrap: [AppComponent], imports: [BrowserModule,
        FormsModule,
        routing], providers: [
        LoginService,
        UserService,
        AppointmentService,
        provideHttpClient(withInterceptorsFromDi())
    ] })
export class AppModule { }
