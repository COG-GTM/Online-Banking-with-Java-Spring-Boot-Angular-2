import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { LoginService } from '../login.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'],
})
export class NavbarComponent {

  loggedIn: boolean;

  constructor(private loginService: LoginService, private router: Router) {
    this.loggedIn = !!localStorage.getItem('PortalAdminHasLoggedIn');
  }

  logout(): void {
    this.loginService.logout().subscribe({
      next: () => localStorage.setItem('PortalAdminHasLoggedIn', ''),
      error: (err) => console.log(err),
    });
    location.reload();
    this.router.navigate(['/login']);
  }

  getDisplay(): string {
    return this.loggedIn ? '' : 'none';
  }
}
