import { Component, OnInit, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Router } from '@angular/router';
import { UserService } from '../user.service';

@Component({
    selector: 'app-user-account',
    templateUrl: './user-account.component.html',
    styleUrls: ['./user-account.component.css']
})
export class UserAccountComponent implements OnInit {
  	private userService = inject(UserService);
  	private router = inject(Router);

  userList = toSignal(
    this.userService.getUsers().pipe(
      map(res => res as any[]),
      catchError(error => { console.log(error); return of([]); })
    ),
    { initialValue: [] as any[] }
  );
	
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
