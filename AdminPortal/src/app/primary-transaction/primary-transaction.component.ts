import { Component, OnInit, inject } from '@angular/core';
import {UserService} from '../user.service';
import { ActivatedRoute, Params } from '@angular/router';
import { DatePipe } from '@angular/common';

@Component({
    selector: 'app-primary-transaction',
    templateUrl: './primary-transaction.component.html',
    styleUrls: ['./primary-transaction.component.css'],
    imports: [DatePipe]
})
export class PrimaryTransactionComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private userService = inject(UserService);


  username:string;
	primaryTransactionList: Object[];

	constructor() {
		this.route.params.forEach((params: Params) => {
     		this.username = params['username'];
		});

		this.getPrimaryTransactionList();
	}

	getPrimaryTransactionList() {
		this.userService.getPrimaryTransactionList(this.username).subscribe(
			res => {
				console.log(res);
        		this.primaryTransactionList = res as Object[];
      		},
      		error => console.log(error)
		)
	}

	ngOnInit() {}

}
