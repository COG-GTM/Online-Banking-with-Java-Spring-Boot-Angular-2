import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { LOGGED_IN_KEY, LoginService } from '../login.service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
})
export class NavbarComponent {
  private readonly loginService = inject(LoginService);

  protected readonly loggedIn = this.loginService.isLoggedIn();

  logout(): void {
    this.loginService.logout().subscribe({
      next: () => {
        localStorage.setItem(LOGGED_IN_KEY, '');
        location.assign('/login');
      },
      error: (err) => {
        console.log(err);
        alert('Logout failed. Please try again.');
      },
    });
  }
}
