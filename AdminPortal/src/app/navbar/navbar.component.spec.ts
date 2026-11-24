import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { NavbarComponent } from './navbar.component';
import { LoginService } from '../login.service';

describe('NavbarComponent', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [NavbarComponent],
      providers: [{ provide: LoginService, useValue: {} }]
    }).compileComponents();
  }));

  afterEach(() => localStorage.removeItem('PortalAdminHasLoggedIn'));

  it('should hide the menu links after logout', () => {
    localStorage.setItem('PortalAdminHasLoggedIn', '');
    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    expect(component.getDisplay()).toBe('none');
  });

  it('should show the menu links when logged in', () => {
    localStorage.setItem('PortalAdminHasLoggedIn', 'true');
    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    expect(component.getDisplay()).toBe('');
  });
});
