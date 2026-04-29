import { Component, OnInit } from '@angular/core';
import { UserService } from '../user.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-primary-transaction',
  templateUrl: './primary-transaction.component.html',
  styleUrls: ['./primary-transaction.component.css']
})
export class PrimaryTransactionComponent implements OnInit {
  username: string;
  primaryTransactionList: any[];

  constructor(private route: ActivatedRoute, private userService: UserService) {
    this.route.params.subscribe(params => {
      this.username = params['username'];
    });
    this.getPrimaryTransactionList();
  }

  getPrimaryTransactionList() {
    this.userService.getPrimaryTransactionList(this.username).subscribe(
      res => {
        this.primaryTransactionList = res;
      },
      error => console.log(error)
    );
  }

  ngOnInit() {}
}
