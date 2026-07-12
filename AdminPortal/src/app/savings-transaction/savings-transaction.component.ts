import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

import { UserService } from '../user.service';
import { Transaction } from '../models';

@Component({
  selector: 'app-savings-transaction',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './savings-transaction.component.html',
  styleUrls: ['./savings-transaction.component.css'],
})
export class SavingsTransactionComponent implements OnInit {

  username = '';
  savingsTransactionList: Transaction[] = [];

  constructor(private route: ActivatedRoute, private userService: UserService) {}

  ngOnInit(): void {
    this.username = this.route.snapshot.paramMap.get('username') ?? '';
    this.getSavingsTransactionList();
  }

  getSavingsTransactionList(): void {
    this.userService.getSavingsTransactionList(this.username).subscribe({
      next: (transactions) => (this.savingsTransactionList = transactions),
      error: (error) => console.log(error),
    });
  }
}
