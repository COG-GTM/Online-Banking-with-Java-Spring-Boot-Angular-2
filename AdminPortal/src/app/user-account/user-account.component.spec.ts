import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';

import { UserAccountComponent } from './user-account.component';
import { UserService } from '../user.service';

const USERS = [
  {
    username: 'john',
    firstName: 'John',
    lastName: 'Doe',
    email: 'john@example.com',
    phone: '111',
    enabled: true,
    primaryAccount: { accountBalance: 100 },
    savingsAccount: { accountBalance: 200 }
  },
  {
    username: 'jane',
    firstName: 'Jane',
    lastName: 'Roe',
    email: 'jane@example.com',
    phone: '222',
    enabled: false,
    primaryAccount: { accountBalance: 300 },
    savingsAccount: { accountBalance: 400 }
  }
];

class MockUserService {
  getUsers() {
    return Observable.of({ _body: JSON.stringify(USERS) });
  }
  enableUser(username: string) {
    return Observable.of({});
  }
  disableUser(username: string) {
    return Observable.of({});
  }
}

describe('UserAccountComponent', () => {
  let component: UserAccountComponent;
  let fixture: ComponentFixture<UserAccountComponent>;
  let router: { navigate: jasmine.Spy };

  beforeEach(async(() => {
    router = { navigate: jasmine.createSpy('navigate') };
    TestBed.configureTestingModule({
      imports: [CommonModule],
      declarations: [UserAccountComponent],
      providers: [
        { provide: UserService, useClass: MockUserService },
        { provide: Router, useValue: router }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserAccountComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should load the user list from the service', () => {
    expect(component.userList.length).toBe(2);
  });

  it('should render a table row per user', () => {
    const rows = fixture.debugElement.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(2);
    expect(fixture.debugElement.nativeElement.textContent).toContain('john');
    expect(fixture.debugElement.nativeElement.textContent).toContain('jane');
  });

  it('onSelectPrimary() should navigate to the primary transaction route', () => {
    component.onSelectPrimary('john');
    expect(router.navigate).toHaveBeenCalledWith(['/primaryTransaction', 'john']);
  });
});
