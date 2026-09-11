import { Component, OnInit } from '@angular/core';
import {UserService, Transaction} from '../user.service';
import { ActivatedRoute, Params } from '@angular/router';

@Component({
  selector: 'app-primary-transaction',
  templateUrl: './primary-transaction.component.html',
  styleUrls: ['./primary-transaction.component.css']
})
export class PrimaryTransactionComponent implements OnInit {

  username:string;
	primaryTransactionList: Transaction[];

	constructor(private route: ActivatedRoute, private userService: UserService) {
		this.route.params.forEach((params: Params) => {
     		this.username = params['username'];
		});

		this.getPrimaryTransactionList();
	}

	getPrimaryTransactionList() {
		this.userService.getPrimaryTransactionList(this.username).subscribe(
			(res: Transaction[]) => {
        		this.primaryTransactionList = res;
      		},
      		error => console.log(error)
		)
	}

	ngOnInit() {}

}
