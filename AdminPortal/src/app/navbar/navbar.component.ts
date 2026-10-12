import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { finalize } from 'rxjs';

import { LoginService } from '../login.service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
})
export class NavbarComponent {
  private readonly loginService = inject(LoginService);
  private readonly router = inject(Router);

  readonly loggedIn = this.loginService.loggedIn;

  logout(): void {
    this.loginService
      .logout()
      .pipe(
        finalize(() => {
          this.loginService.markLoggedOut();
          this.router.navigate(['/login']);
        }),
      )
      .subscribe({ error: (err) => console.log(err) });
  }

  getDisplay(): string {
    return this.loggedIn() ? '' : 'none';
  }
}
