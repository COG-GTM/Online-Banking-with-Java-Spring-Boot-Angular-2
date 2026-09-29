import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';

import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
  });

  it('logs in and navigates to the user account page', async () => {
    const router = TestBed.inject(Router);
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(LoginComponent);
    await fixture.whenStable();

    fixture.componentInstance.username = 'admin';
    fixture.componentInstance.password = 'secret';
    fixture.componentInstance.onSubmit();
    TestBed.inject(HttpTestingController)
      .expectOne('http://localhost:8080/index')
      .flush('<html></html>');
    await fixture.whenStable();

    expect(navigate).toHaveBeenCalledWith(['/userAccount']);
    expect(fixture.componentInstance.loggedIn()).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Welcome to Admin Portal!');
  });
});
