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
	
	constructor(private userService: UserService, private router: Router) {
		this.getUsers();
	}

	getUsers() {
		this.userService.getUsers().subscribe(
			res => {
        		this.userList = res.json();
      		},
      		error => console.log(error)
		)
	}

	onSelectPrimary(username: string) {
    	this.router.navigate(['/primaryTransaction', username]);
  	}	

  	onSelectSavings(username: string) {
    	this.router.navigate(['/savingsTransaction', username]);
  	}	

  	enableUser(username: string) {
  		this.userService.enableUser(username).subscribe(
  			() => this.setEnabled(username, true),
  			error => console.log(error)
  		);
  	}

  	disableUser(username: string) {
  		this.userService.disableUser(username).subscribe(
  			() => this.setEnabled(username, false),
  			error => console.log(error)
  		);
  	}

  	private setEnabled(username: string, enabled: boolean) {
  		const user = this.userList.find(u => u['username'] === username);
  		if (user) {
  			user['enabled'] = enabled;
  		}
  	}


  ngOnInit() {
  }

}
