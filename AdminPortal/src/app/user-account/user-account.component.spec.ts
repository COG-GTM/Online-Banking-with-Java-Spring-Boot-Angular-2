import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';

import { UserAccountComponent } from './user-account.component';
import { UserService } from '../user.service';

describe('UserAccountComponent', () => {
  let fixture: ComponentFixture<UserAccountComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [UserAccountComponent],
      providers: [{ provide: UserService, useValue: { getUsers: () => Observable.of({ _body: JSON.stringify([{ username: "alice", firstName: "Alice", primaryAccount: { accountBalance: 10 }, savingsAccount: { accountBalance: 20 }, enabled: true }, { username: "bob", firstName: "Bob", primaryAccount: { accountBalance: 0 }, savingsAccount: { accountBalance: 0 }, enabled: false }]) }) } }],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  }));

  it('should render one row per item', () => {
    fixture = TestBed.createComponent(UserAccountComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(2);
    expect(fixture.nativeElement.textContent).toContain('Alice');
  });
});
