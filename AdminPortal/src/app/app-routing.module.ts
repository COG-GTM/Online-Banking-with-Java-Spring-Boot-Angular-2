import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', loadChildren: './auth/auth.module#AuthModule' },
  { path: 'userAccount', loadChildren: './user/user.module#UserModule' },
  { path: 'transaction', loadChildren: './transaction/transaction.module#TransactionModule' },
  { path: 'appointment', loadChildren: './appointment/appointment.module#AppointmentModule' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
