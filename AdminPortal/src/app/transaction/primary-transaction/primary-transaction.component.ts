import { Component, OnInit } from '@angular/core';
import {TransactionService} from '../transaction.service';
import { ActivatedRoute, Params } from '@angular/router';

@Component({
  selector: 'app-primary-transaction',
  templateUrl: './primary-transaction.component.html',
  styleUrls: ['./primary-transaction.component.css']
})
export class PrimaryTransactionComponent implements OnInit {

  username:string;
	primaryTransactionList: Object[];

	constructor(private route: ActivatedRoute, private transactionService: TransactionService) {
		this.route.params.forEach((params: Params) => {
     		this.username = params['username'];
		});

		this.getPrimaryTransactionList();
	}

	getPrimaryTransactionList() {
		this.transactionService.getPrimaryTransactionList(this.username).subscribe(
			res => {
				console.log(JSON.parse(JSON.stringify(res))._body);
        		this.primaryTransactionList = JSON.parse(JSON.parse(JSON.stringify(res))._body);
      		},
      		error => console.log(error)
		)
	}

	ngOnInit() {}

}
