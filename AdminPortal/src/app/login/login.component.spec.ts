import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { LoginService } from '../login.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  const loginService = {
    sendCredential: vi.fn(() => of('')),
    isLoggedIn: vi.fn(() => false),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [{ provide: LoginService, useValue: loginService }],
    });
  });

  it('shows the login form when logged out', async () => {
    const fixture = TestBed.createComponent(LoginComponent);
    await fixture.whenStable();
    const form: HTMLFormElement = fixture.nativeElement.querySelector('form');
    expect(form.hidden).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('Please login');
  });

  it('shows the welcome message when logged in', async () => {
    loginService.isLoggedIn.mockReturnValueOnce(true);
    const fixture = TestBed.createComponent(LoginComponent);
    await fixture.whenStable();
    const form: HTMLFormElement = fixture.nativeElement.querySelector('form');
    expect(form.hidden).toBe(true);
  });
});
