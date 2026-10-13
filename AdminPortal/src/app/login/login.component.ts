import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LOGGED_IN_KEY, LoginService } from '../login.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private readonly loginService = inject(LoginService);

  protected readonly loggedIn = signal(this.loginService.isLoggedIn());
  protected username = '';
  protected password = '';

  onSubmit(): void {
    this.loginService.sendCredential(this.username, this.password).subscribe({
      next: () => {
        this.loggedIn.set(true);
        localStorage.setItem(LOGGED_IN_KEY, 'true');
        location.reload();
      },
      error: (err) => console.log(err),
    });
  }
}
