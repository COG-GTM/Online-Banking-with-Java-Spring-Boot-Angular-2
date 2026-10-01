import { Component, OnInit } from '@angular/core';
import {UserService} from '../user.service';
import { ActivatedRoute, Params } from '@angular/router';

@Component({
  selector: 'app-primary-transaction',
  templateUrl: './primary-transaction.component.html',
  styleUrls: ['./primary-transaction.component.css']
})
export class PrimaryTransactionComponent implements OnInit {

  username:string;
	primaryTransactionList: Object[];
	page: number = 0;
	totalPages: number = 0;
	totalCount: number = 0;

	constructor(private route: ActivatedRoute, private userService: UserService) {
		this.route.params.forEach((params: Params) => {
     		this.username = params['username'];
		});

		this.getPrimaryTransactionList();
	}

	getPrimaryTransactionList() {
		this.userService.getPrimaryTransactionList(this.username, this.page).subscribe(
			res => {
        		this.primaryTransactionList = res.json();
        		this.totalPages = Number(res.headers.get('X-Total-Pages')) || 0;
        		this.totalCount = Number(res.headers.get('X-Total-Count')) || 0;
      		},
      		error => console.log(error)
		)
	}

	goToPage(page: number) {
		if (page < 0 || page >= this.totalPages) {
			return;
		}
		this.page = page;
		this.getPrimaryTransactionList();
	}

	ngOnInit() {}

}
