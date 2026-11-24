import { Component, OnInit, inject, signal } from '@angular/core';
import {LoginService} from '../login.service';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css'],
    imports: [FormsModule]
})
export class LoginComponent implements OnInit {
  private loginService = inject(LoginService);


  loggedIn = signal(!(localStorage.getItem('PortalAdminHasLoggedIn') == '' || localStorage.getItem('PortalAdminHasLoggedIn') == null));
  username = signal('');
  password = signal('');

  
  onSubmit() {
  	this.loginService.sendCredential(this.username(), this.password()).subscribe(
      res => {
        this.loggedIn.set(true);
        localStorage.setItem('PortalAdminHasLoggedIn', 'true');
        location.reload();
      },
      err => console.log(err)
    );
  }

  ngOnInit() {}

}
