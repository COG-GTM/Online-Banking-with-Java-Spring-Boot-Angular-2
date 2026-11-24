import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { SavingsTransactionComponent } from './savings-transaction.component';
import { UserService } from '../user.service';

describe('SavingsTransactionComponent', () => {
  let fixture: ComponentFixture<SavingsTransactionComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [SavingsTransactionComponent],
      providers: [{ provide: UserService, useValue: { getSavingsTransactionList: () => of([{ date: 0, description: "Withdraw", type: "Account", status: "Finished", amount: 5, availableBalance: 95 }]) } }],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  }));

  it('should render one row per item', () => {
    fixture = TestBed.createComponent(SavingsTransactionComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(1);
    expect(fixture.nativeElement.textContent).toContain('Withdraw');
  });
});
