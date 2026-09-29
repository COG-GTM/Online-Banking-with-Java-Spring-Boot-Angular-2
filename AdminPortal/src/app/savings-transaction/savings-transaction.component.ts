import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

import { Transaction } from '../models';
import { UserService } from '../user.service';

@Component({
  selector: 'app-savings-transaction',
  imports: [DatePipe],
  templateUrl: './savings-transaction.component.html',
  styleUrl: './savings-transaction.component.css',
})
export class SavingsTransactionComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly userService = inject(UserService);

  readonly username = this.route.snapshot.paramMap.get('username') ?? '';
  readonly savingsTransactionList = signal<Transaction[]>([]);

  constructor() {
    this.getSavingsTransactionList();
  }

  getSavingsTransactionList(): void {
    this.userService.getSavingsTransactionList(this.username).subscribe({
      next: (transactions) => this.savingsTransactionList.set(transactions),
      error: (error) => console.log(error),
    });
  }
}
