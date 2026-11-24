import { enableProdMode, provideZoneChangeDetection, importProvidersFrom } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';


import { environment } from './environments/environment';
import { LoginService } from './app/login.service';
import { UserService } from './app/user.service';
import { AppointmentService } from './app/appointment.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { BrowserModule, bootstrapApplication } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { routing } from './app/app.routing';
import { AppComponent } from './app/app.component';

if (environment.production) {
  enableProdMode();
}

bootstrapApplication(AppComponent, {
    providers: [
        provideZoneChangeDetection(),
        importProvidersFrom(BrowserModule, FormsModule, routing),
        LoginService,
        UserService,
        AppointmentService,
        provideHttpClient(withInterceptorsFromDi())
    ]
});
