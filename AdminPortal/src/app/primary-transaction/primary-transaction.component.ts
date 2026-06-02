import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Params } from '@angular/router';

import { UserService } from '../user.service';

@Component({
  selector: 'app-primary-transaction',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './primary-transaction.component.html',
  styleUrls: ['./primary-transaction.component.css']
})
export class PrimaryTransactionComponent {

  username = '';
  primaryTransactionList: any[] = [];

  constructor(private route: ActivatedRoute, private userService: UserService) {
    this.route.params.forEach((params: Params) => {
      this.username = params['username'];
    });
    this.getPrimaryTransactionList();
  }

  getPrimaryTransactionList(): void {
    this.userService.getPrimaryTransactionList(this.username).subscribe({
      next: (res) => this.primaryTransactionList = res,
      error: (error) => console.log(error)
    });
  }
}
