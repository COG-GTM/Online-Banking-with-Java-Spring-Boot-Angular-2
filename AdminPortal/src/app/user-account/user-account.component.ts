import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
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

  protected readonly userList = signal<User[]>([]);

  constructor() {
    this.getUsers();
  }

  getUsers(): void {
    this.userService.getUsers().subscribe({
      next: (users) => this.userList.set(users),
      error: (err) => console.log(err),
    });
  }

  onSelectPrimary(username: string): void {
    this.router.navigate(['/primaryTransaction', username]);
  }

  onSelectSavings(username: string): void {
    this.router.navigate(['/savingsTransaction', username]);
  }

  enableUser(username: string): void {
    this.refreshAfter(this.userService.enableUser(username));
  }

  disableUser(username: string): void {
    this.refreshAfter(this.userService.disableUser(username));
  }

  private refreshAfter(request: Observable<unknown>): void {
    request.subscribe({
      next: () => this.getUsers(),
      error: (err) => console.log(err),
    });
  }
}
