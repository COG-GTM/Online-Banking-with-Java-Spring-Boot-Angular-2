import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

import { Transaction } from '../models';
import { UserService } from '../user.service';

@Component({
  selector: 'app-primary-transaction',
  imports: [DatePipe],
  templateUrl: './primary-transaction.component.html',
  styleUrl: './primary-transaction.component.css',
})
export class PrimaryTransactionComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly userService = inject(UserService);

  readonly username = this.route.snapshot.paramMap.get('username') ?? '';
  readonly primaryTransactionList = signal<Transaction[]>([]);

  constructor() {
    this.getPrimaryTransactionList();
  }

  getPrimaryTransactionList(): void {
    this.userService.getPrimaryTransactionList(this.username).subscribe({
      next: (transactions) => this.primaryTransactionList.set(transactions),
      error: (error) => console.log(error),
    });
  }
}
