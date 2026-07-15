import { ModuleWithProviders }  from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { LoginComponent } from './login/login.component';
import { UserAccountComponent } from './user-account/user-account.component';
import { PrimaryTransactionComponent } from './primary-transaction/primary-transaction.component';
import { SavingsTransactionComponent } from './savings-transaction/savings-transaction.component';
import { AppointmentComponent } from './appointment/appointment.component';
import { AuthGuard } from './auth.guard';




const appRoutes: Routes = [
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
  {
  	path: 'login',
  	component: LoginComponent
  },
  {
    path: 'userAccount',
    component: UserAccountComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'primaryTransaction/:username',
    component: PrimaryTransactionComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'savingsTransaction/:username',
    component: SavingsTransactionComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'appointment',
    component: AppointmentComponent,
    canActivate: [AuthGuard]
  }
];

export const routing: ModuleWithProviders = RouterModule.forRoot(appRoutes);