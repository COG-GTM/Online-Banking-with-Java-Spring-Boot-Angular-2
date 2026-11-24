import { Component, OnInit, inject } from '@angular/core';
import { LoginService } from '../login.service';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
    selector: 'app-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.css'],
    imports: [RouterLink, RouterLinkActive]
})
export class NavbarComponent implements OnInit {
  private loginService = inject(LoginService);
  private router = inject(Router);


  loggedIn: boolean;

	constructor() {
		if(localStorage.getItem('PortalAdminHasLoggedIn') == '') {
			this.loggedIn = false;
		} else {
			this.loggedIn = true;
		}
	}

	logout(){
		this.loginService.logout().subscribe(
			res => {
				localStorage.setItem('PortalAdminHasLoggedIn', '');
			},
			err => console.log(err)
			);
		location.reload();
		this.router.navigate(['/login']);
	}

	getDisplay() {
    if(!this.loggedIn){
      return "none";
    } else {
      return "";
    }
  }

  ngOnInit() {
  }

}
