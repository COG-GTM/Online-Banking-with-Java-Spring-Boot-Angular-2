import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { UserService } from '../user.service';

@Component({
  selector: 'app-user-account',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-account.component.html',
  styleUrls: ['./user-account.component.css']
})
export class UserAccountComponent {

  userList: any[] = [];

  constructor(private userService: UserService, private router: Router) {
    this.getUsers();
  }

  getUsers(): void {
    this.userService.getUsers().subscribe({
      next: (res) => this.userList = res,
      error: (error) => console.log(error)
    });
  }

  onSelectPrimary(username: string): void {
    this.router.navigate(['/primaryTransaction', username]);
  }

  onSelectSavings(username: string): void {
    this.router.navigate(['/savingsTransaction', username]);
  }

  enableUser(username: string): void {
    this.userService.enableUser(username).subscribe({
      complete: () => location.reload()
    });
  }

  disableUser(username: string): void {
    this.userService.disableUser(username).subscribe({
      complete: () => location.reload()
    });
  }
}
