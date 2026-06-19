import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';

import { NavbarComponent } from './navbar.component';
import { LoginService } from '../login.service';

class MockLoginService {
  logout() {
    return Observable.of({});
  }
}

describe('NavbarComponent', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;
  let router: { navigate: jasmine.Spy };

  beforeEach(async(() => {
    router = { navigate: jasmine.createSpy('navigate') };
    TestBed.configureTestingModule({
      declarations: [NavbarComponent],
      providers: [
        { provide: LoginService, useClass: MockLoginService },
        { provide: Router, useValue: router }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('getDisplay() should hide the navbar when the user is not logged in', () => {
    component.loggedIn = false;
    expect(component.getDisplay()).toBe('none');
  });

  it('getDisplay() should show the navbar when the user is logged in', () => {
    component.loggedIn = true;
    expect(component.getDisplay()).toBe('');
  });
});
