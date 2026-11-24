import { Routes } from '@angular/router';

export const appRoutes: Routes = [
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'userAccount',
    loadComponent: () => import('./user-account/user-account.component').then(m => m.UserAccountComponent)
  },
  {
    path: 'primaryTransaction/:username',
    loadComponent: () => import('./primary-transaction/primary-transaction.component').then(m => m.PrimaryTransactionComponent)
  },
  {
    path: 'savingsTransaction/:username',
    loadComponent: () => import('./savings-transaction/savings-transaction.component').then(m => m.SavingsTransactionComponent)
  },
  {
    path: 'appointment',
    loadComponent: () => import('./appointment/appointment.component').then(m => m.AppointmentComponent)
  }
];
