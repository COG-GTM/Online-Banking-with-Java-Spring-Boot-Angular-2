import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { User } from '../models';
import { UserService } from '../user.service';

@Component({
  selector: 'app-user-account',
  templateUrl: './user-account.component.html',
  styleUrl: './user-account.component.css',
})
export class UserAccountComponent {
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);

  readonly userList = signal<User[]>([]);

  constructor() {
    this.getUsers();
  }

  getUsers(): void {
    this.userService.getUsers().subscribe({
      next: (users) => this.userList.set(users),
      error: (error) => console.log(error),
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
      complete: () => this.getUsers(),
      error: (error) => console.log(error),
    });
  }

  disableUser(username: string): void {
    this.userService.disableUser(username).subscribe({
      complete: () => this.getUsers(),
      error: (error) => console.log(error),
    });
  }
}
