import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { LoginService } from '../login.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private readonly loginService = inject(LoginService);
  private readonly router = inject(Router);

  readonly loggedIn = this.loginService.loggedIn;
  username = '';
  password = '';

  onSubmit(): void {
    this.loginService.sendCredential(this.username, this.password).subscribe({
      next: () => {
        this.loginService.markLoggedIn();
        this.router.navigate(['/userAccount']);
      },
      error: (err) => console.log(err),
    });
  }
}
