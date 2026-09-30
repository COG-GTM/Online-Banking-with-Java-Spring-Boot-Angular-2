import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { LoginService } from '../login.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {

  loggedIn = false;
  username = '';
  password = '';

  constructor(private loginService: LoginService) {
    const flag = localStorage.getItem('PortalAdminHasLoggedIn');
    this.loggedIn = !(flag === '' || flag == null);
  }

  onSubmit(): void {
    this.loginService.sendCredential(this.username, this.password).subscribe({
      next: () => {
        this.loggedIn = true;
        localStorage.setItem('PortalAdminHasLoggedIn', 'true');
        location.reload();
      },
      error: (err) => console.log(err)
    });
  }
}
