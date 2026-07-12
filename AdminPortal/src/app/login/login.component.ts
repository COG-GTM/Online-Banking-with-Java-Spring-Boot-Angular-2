import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { LoginService } from '../login.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent {

  loggedIn: boolean;
  username = '';
  password = '';

  constructor(private loginService: LoginService) {
    this.loggedIn = !!localStorage.getItem('PortalAdminHasLoggedIn');
  }

  onSubmit(): void {
    this.loginService.sendCredential(this.username, this.password).subscribe({
      next: () => {
        this.loggedIn = true;
        localStorage.setItem('PortalAdminHasLoggedIn', 'true');
        location.reload();
      },
      error: (err) => console.log(err),
    });
  }
}
