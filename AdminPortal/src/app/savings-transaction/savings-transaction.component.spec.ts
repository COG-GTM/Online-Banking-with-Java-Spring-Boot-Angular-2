import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';
import { UserService } from '../user.service';
import { SavingsTransactionComponent } from './savings-transaction.component';

describe('SavingsTransactionComponent', () => {
  const userService = {
    getSavingsTransactionList: vi.fn(() =>
      of([
        {
          id: 1,
          date: '2017-06-15T00:00:00',
          description: 'Deposit',
          type: 'Account',
          status: 'Finished',
          amount: 50,
          availableBalance: 150,
        },
      ]),
    ),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([{ path: 'savings/:username', component: SavingsTransactionComponent }]),
        { provide: UserService, useValue: userService },
      ],
    });
  });

  it('loads transactions for the username route param', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/savings/jdoe', SavingsTransactionComponent);
    expect(userService.getSavingsTransactionList).toHaveBeenCalledWith('jdoe');
    const text = harness.routeNativeElement?.textContent ?? '';
    expect(text).toContain('Savings Account Transaction List');
    expect(text).toContain('Deposit');
    expect(text).toContain('06/15/2017');
  });
});
