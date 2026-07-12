import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { UserService } from '../user.service';
import { User } from '../models';

@Component({
  selector: 'app-user-account',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-account.component.html',
  styleUrls: ['./user-account.component.css'],
})
export class UserAccountComponent implements OnInit {

  userList: User[] = [];

  constructor(private userService: UserService, private router: Router) {}

  ngOnInit(): void {
    this.getUsers();
  }

  getUsers(): void {
    this.userService.getUsers().subscribe({
      next: (users) => (this.userList = users),
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
      next: () => location.reload(),
      error: (error) => console.log(error),
    });
  }

  disableUser(username: string): void {
    this.userService.disableUser(username).subscribe({
      next: () => location.reload(),
      error: (error) => console.log(error),
    });
  }
}
