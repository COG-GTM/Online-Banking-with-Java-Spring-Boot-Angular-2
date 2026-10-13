import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { LOGGED_IN_KEY, LoginService } from '../login.service';
import { NavbarComponent } from './navbar.component';

describe('NavbarComponent', () => {
  const loginService = { logout: vi.fn(() => of('')), isLoggedIn: vi.fn(() => false) };

  function render() {
    TestBed.configureTestingModule({
      imports: [NavbarComponent],
      providers: [provideRouter([]), { provide: LoginService, useValue: loginService }],
    });
    const fixture = TestBed.createComponent(NavbarComponent);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  afterEach(() => localStorage.clear());

  it('hides admin links when logged out', () => {
    loginService.isLoggedIn.mockReturnValue(false);
    const el = render();
    expect(el.textContent).toContain('Admin Portal');
    expect(el.textContent).not.toContain('User Account');
    expect(el.textContent).not.toContain('Logout');
  });

  it('shows admin links when logged in', () => {
    loginService.isLoggedIn.mockReturnValue(true);
    const el = render();
    expect(el.textContent).toContain('User Account');
    expect(el.textContent).toContain('Appointment');
    expect(el.textContent).toContain('Logout');
  });

  it('clears the logged-in flag after logout completes', () => {
    loginService.isLoggedIn.mockReturnValue(true);
    localStorage.setItem(LOGGED_IN_KEY, 'true');
    const assign = vi.fn();
    vi.stubGlobal('location', { ...window.location, assign });
    const fixture = TestBed.configureTestingModule({
      imports: [NavbarComponent],
      providers: [provideRouter([]), { provide: LoginService, useValue: loginService }],
    }).createComponent(NavbarComponent);
    fixture.componentInstance.logout();
    expect(loginService.logout).toHaveBeenCalled();
    expect(localStorage.getItem(LOGGED_IN_KEY)).toBe('');
    expect(assign).toHaveBeenCalledWith('/login');
    vi.unstubAllGlobals();
  });
});
