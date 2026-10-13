import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { User } from '../models';
import { UserService } from '../user.service';
import { UserAccountComponent } from './user-account.component';

const users: User[] = [
  {
    userId: 1,
    username: 'jdoe',
    firstName: 'John',
    lastName: 'Doe',
    email: 'jdoe@example.com',
    phone: '555',
    enabled: true,
    primaryAccount: { id: 1, accountNumber: 11, accountBalance: 100 },
    savingsAccount: { id: 2, accountNumber: 22, accountBalance: 200 },
  },
];

describe('UserAccountComponent', () => {
  const userService = {
    getUsers: vi.fn(() => of(users)),
    disableUser: vi.fn(() => of('')),
    enableUser: vi.fn(() => of('')),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      imports: [UserAccountComponent],
      providers: [provideRouter([]), { provide: UserService, useValue: userService }],
    });
  });

  it('renders the user list', async () => {
    const fixture = TestBed.createComponent(UserAccountComponent);
    await fixture.whenStable();
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('jdoe');
    expect(text).toContain('100');
    expect(text).toContain('Disable');
  });

  it('disables a user and refreshes the list', async () => {
    const fixture = TestBed.createComponent(UserAccountComponent);
    await fixture.whenStable();
    fixture.componentInstance.disableUser('jdoe');
    expect(userService.disableUser).toHaveBeenCalledWith('jdoe');
    expect(userService.getUsers).toHaveBeenCalledTimes(2);
  });

  it('navigates to the primary transaction list', async () => {
    const fixture = TestBed.createComponent(UserAccountComponent);
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.componentInstance.onSelectPrimary('jdoe');
    expect(navigate).toHaveBeenCalledWith(['/primaryTransaction', 'jdoe']);
  });
});
