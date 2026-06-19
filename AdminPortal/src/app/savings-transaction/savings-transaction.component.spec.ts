import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';

import { SavingsTransactionComponent } from './savings-transaction.component';
import { UserService } from '../user.service';

const TRANSACTIONS = [
  {
    date: '2020-01-01T10:00:00',
    description: 'Deposit to savings Account',
    type: 'Account',
    status: 'Finished',
    amount: 100,
    availableBalance: 100
  },
  {
    date: '2020-01-02T10:00:00',
    description: 'Withdraw from savings Account',
    type: 'Account',
    status: 'Finished',
    amount: 40,
    availableBalance: 60
  }
];

class MockUserService {
  getSavingsTransactionList(username: string) {
    return Observable.of({ _body: JSON.stringify(TRANSACTIONS) });
  }
}

describe('SavingsTransactionComponent', () => {
  let component: SavingsTransactionComponent;
  let fixture: ComponentFixture<SavingsTransactionComponent>;
  let userService: MockUserService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [CommonModule],
      declarations: [SavingsTransactionComponent],
      providers: [
        { provide: UserService, useClass: MockUserService },
        { provide: ActivatedRoute, useValue: { params: Observable.of({ username: 'jane' }) } }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SavingsTransactionComponent);
    component = fixture.componentInstance;
    userService = TestBed.get(UserService);
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should read the username from the route params', () => {
    expect(component.username).toBe('jane');
  });

  it('should request the savings transaction list for that username', () => {
    spyOn(userService, 'getSavingsTransactionList').and.callThrough();
    component.getSavingsTransactionList();
    expect(userService.getSavingsTransactionList).toHaveBeenCalledWith('jane');
  });

  it('should render a table row per transaction', () => {
    const rows = fixture.debugElement.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(2);
    expect(fixture.debugElement.nativeElement.textContent).toContain('Deposit to savings Account');
  });
});
