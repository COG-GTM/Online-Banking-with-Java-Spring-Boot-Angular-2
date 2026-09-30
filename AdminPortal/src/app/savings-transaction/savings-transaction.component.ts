import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Params } from '@angular/router';

import { UserService } from '../user.service';

@Component({
  selector: 'app-savings-transaction',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './savings-transaction.component.html',
  styleUrls: ['./savings-transaction.component.css']
})
export class SavingsTransactionComponent {

  username = '';
  savingsTransactionList: any[] = [];

  constructor(private route: ActivatedRoute, private userService: UserService) {
    this.route.params.forEach((params: Params) => {
      this.username = params['username'];
    });
    this.getSavingsTransactionList();
  }

  getSavingsTransactionList(): void {
    this.userService.getSavingsTransactionList(this.username).subscribe({
      next: (res) => this.savingsTransactionList = res,
      error: (error) => console.log(error)
    });
  }
}
