import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

import { UserService } from '../user.service';
import { Transaction } from '../models';

@Component({
  selector: 'app-primary-transaction',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './primary-transaction.component.html',
  styleUrls: ['./primary-transaction.component.css'],
})
export class PrimaryTransactionComponent implements OnInit {

  username = '';
  primaryTransactionList: Transaction[] = [];

  constructor(private route: ActivatedRoute, private userService: UserService) {}

  ngOnInit(): void {
    this.username = this.route.snapshot.paramMap.get('username') ?? '';
    this.getPrimaryTransactionList();
  }

  getPrimaryTransactionList(): void {
    this.userService.getPrimaryTransactionList(this.username).subscribe({
      next: (transactions) => (this.primaryTransactionList = transactions),
      error: (error) => console.log(error),
    });
  }
}
