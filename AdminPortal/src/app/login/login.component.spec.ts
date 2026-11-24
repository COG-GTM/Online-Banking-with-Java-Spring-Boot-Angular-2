import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';

import { LoginComponent } from './login.component';
import { LoginService } from '../login.service';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
    imports: [FormsModule, LoginComponent],
    providers: [{ provide: LoginService, useValue: {} }]
}).compileComponents();
  }));

  afterEach(() => localStorage.removeItem('PortalAdminHasLoggedIn'));

  it('should show the login form when not logged in', () => {
    localStorage.removeItem('PortalAdminHasLoggedIn');
    fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.loggedIn()).toBe(false);
    expect(fixture.nativeElement.querySelector('form').hidden).toBe(false);
  });

  it('should show the welcome message when logged in', () => {
    localStorage.setItem('PortalAdminHasLoggedIn', 'true');
    fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.loggedIn()).toBe(true);
    expect(fixture.nativeElement.querySelector('form').hidden).toBe(true);
  });
});
