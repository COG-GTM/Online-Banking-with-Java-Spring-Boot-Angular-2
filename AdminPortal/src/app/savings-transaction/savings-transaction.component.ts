import { Component, OnInit } from '@angular/core';
import {UserService} from '../user.service';
import { ActivatedRoute, Params } from '@angular/router';


@Component({
  selector: 'app-savings-transaction',
  standalone: false,
  templateUrl: './savings-transaction.component.html',
  styleUrls: ['./savings-transaction.component.css']
})
export class SavingsTransactionComponent implements OnInit {

  username:string;
	savingsTransactionList: any[];

	constructor(private route: ActivatedRoute, private userService: UserService) {
		this.route.params.forEach((params: Params) => {
     		this.username = params['username'];
		});

		this.getSavingsTransactionList();
	}

	getSavingsTransactionList() {
		this.userService.getSavingsTransactionList(this.username).subscribe(
			res => {
				console.log(res);
        		this.savingsTransactionList = res;
      		},
      		error => console.log(error)
		)
	}

	ngOnInit() {}
}
