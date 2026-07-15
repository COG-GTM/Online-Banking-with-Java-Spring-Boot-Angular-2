import { Component, OnInit } from '@angular/core';
import { LoginService } from '../login.service';
import { AuthService } from '../auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {

  loggedIn: boolean;

	constructor(private loginService: LoginService, private authService: AuthService, private router: Router) {
	}

	logout(){
		this.loginService.logout().subscribe(
			res => {
				this.authService.setLoggedIn(false);
				this.router.navigate(['/login']);
			},
			err => {
				console.log(err);
				this.authService.setLoggedIn(false);
				this.router.navigate(['/login']);
			}
			);
	}

	getDisplay() {
    if(!this.loggedIn){
      return "none";
    } else {
      return "";
    }
  }

  ngOnInit() {
    this.authService.isLoggedIn$.subscribe(loggedIn => this.loggedIn = loggedIn);
  }

}
