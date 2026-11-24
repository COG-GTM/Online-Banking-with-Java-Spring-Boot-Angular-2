import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { UserAccountComponent } from './user-account.component';
import { UserService } from '../user.service';

describe('UserAccountComponent', () => {
  let fixture: ComponentFixture<UserAccountComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
    imports: [RouterTestingModule, UserAccountComponent],
    providers: [{ provide: UserService, useValue: { getUsers: () => of([{ username: "alice", firstName: "Alice", primaryAccount: { accountBalance: 10 }, savingsAccount: { accountBalance: 20 }, enabled: true }, { username: "bob", firstName: "Bob", primaryAccount: { accountBalance: 0 }, savingsAccount: { accountBalance: 0 }, enabled: false }]) } }],
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
