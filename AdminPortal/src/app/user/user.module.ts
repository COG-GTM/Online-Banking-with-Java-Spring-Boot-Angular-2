import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpModule } from '@angular/http';
import { UserRoutingModule } from './user-routing.module';
import { UserAccountComponent } from './user-account/user-account.component';
import { UserService } from './user.service';

@NgModule({
  declarations: [UserAccountComponent],
  imports: [CommonModule, HttpModule, UserRoutingModule],
  providers: [UserService]
})
export class UserModule { }
