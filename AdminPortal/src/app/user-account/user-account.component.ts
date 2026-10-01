import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from '../user.service';

@Component({
  selector: 'app-user-account',
  templateUrl: './user-account.component.html',
  styleUrls: ['./user-account.component.css']
})
export class UserAccountComponent implements OnInit {

  	userList: Object[];
	page: number = 0;
	pageSize: number = 50;
	totalPages: number = 0;
	totalUsers: number = 0;
	
	constructor(private userService: UserService, private router: Router) {
		this.getUsers();
	}

	getUsers() {
		this.userService.getUsers(this.page, this.pageSize).subscribe(
			res => {
        		this.userList = res.json();
        		this.totalUsers = Number(res.headers.get('X-Total-Count')) || 0;
        		this.totalPages = Number(res.headers.get('X-Total-Pages')) || 0;
      		},
      		error => console.log(error)
		)
	}

	goToPage(page: number) {
		if (page < 0 || page >= this.totalPages) {
			return;
		}
		this.page = page;
		this.getUsers();
	}

	onSelectPrimary(username: string) {
    	this.router.navigate(['/primaryTransaction', username]);
  	}	

  	onSelectSavings(username: string) {
    	this.router.navigate(['/savingsTransaction', username]);
  	}	

  	enableUser(username: string) {
  		this.userService.enableUser(username).subscribe();
  		location.reload();
  	}

  	disableUser(username: string) {
  		this.userService.disableUser(username).subscribe();
  		location.reload();
  	}


  ngOnInit() {
  }

}
