import { Component, OnInit } from '@angular/core';
import {LoginService} from '../login.service';
import {AuthService} from '../auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  loggedIn: boolean;
  username: string;
  password: string;

	constructor (private loginService: LoginService, private authService: AuthService) {
    this.loggedIn = this.authService.isLoggedIn;
  }
  
  onSubmit() {
  	this.loginService.sendCredential(this.username, this.password).subscribe(
      res => {
        this.authService.setLoggedIn(true);
        this.loggedIn = true;
      },
      err => console.log(err)
    );
  }

  ngOnInit() {}

}
