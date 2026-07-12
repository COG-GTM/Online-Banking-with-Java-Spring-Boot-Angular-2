import { Routes } from '@angular/router';

import { LoginComponent } from './login/login.component';
import { UserAccountComponent } from './user-account/user-account.component';
import { PrimaryTransactionComponent } from './primary-transaction/primary-transaction.component';
import { SavingsTransactionComponent } from './savings-transaction/savings-transaction.component';
import { AppointmentComponent } from './appointment/appointment.component';
import { authGuard } from './auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'userAccount', component: UserAccountComponent, canActivate: [authGuard] },
  {
    path: 'primaryTransaction/:username',
    component: PrimaryTransactionComponent,
    canActivate: [authGuard],
  },
  {
    path: 'savingsTransaction/:username',
    component: SavingsTransactionComponent,
    canActivate: [authGuard],
  },
  { path: 'appointment', component: AppointmentComponent, canActivate: [authGuard] },
];
