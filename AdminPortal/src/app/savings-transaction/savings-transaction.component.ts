import { Component, OnInit } from '@angular/core';
import {UserService} from '../user.service';
import { ActivatedRoute, Params } from '@angular/router';


@Component({
  selector: 'app-savings-transaction',
  templateUrl: './savings-transaction.component.html',
  styleUrls: ['./savings-transaction.component.css']
})
export class SavingsTransactionComponent implements OnInit {

  username:string;
	savingsTransactionList: Object[];
	page: number = 0;
	totalPages: number = 0;
	totalCount: number = 0;

	constructor(private route: ActivatedRoute, private userService: UserService) {
		this.route.params.forEach((params: Params) => {
     		this.username = params['username'];
		});

		this.getSavingsTransactionList();
	}

	getSavingsTransactionList() {
		this.userService.getSavingsTransactionList(this.username, this.page).subscribe(
			res => {
        		this.savingsTransactionList = res.json();
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
		this.getSavingsTransactionList();
	}

	ngOnInit() {}
}
