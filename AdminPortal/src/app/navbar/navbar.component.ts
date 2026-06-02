import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { LoginService } from '../login.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {

  loggedIn = false;

  constructor(private loginService: LoginService, private router: Router) {
    const flag = localStorage.getItem('PortalAdminHasLoggedIn');
    this.loggedIn = !(flag === '' || flag == null);
  }

  logout(): void {
    this.loginService.logout().subscribe({
      next: () => localStorage.setItem('PortalAdminHasLoggedIn', ''),
      error: (err) => console.log(err)
    });
    location.reload();
    this.router.navigate(['/login']);
  }

  getDisplay(): string {
    return this.loggedIn ? '' : 'none';
  }
}
