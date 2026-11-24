import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { PrimaryTransactionComponent } from './primary-transaction.component';
import { UserService } from '../user.service';

describe('PrimaryTransactionComponent', () => {
  let fixture: ComponentFixture<PrimaryTransactionComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
    imports: [RouterTestingModule, PrimaryTransactionComponent],
    providers: [{ provide: UserService, useValue: { getPrimaryTransactionList: () => of([{ date: 0, description: "Deposit", type: "Account", status: "Finished", amount: 100, availableBalance: 100 }]) } }],
    schemas: [NO_ERRORS_SCHEMA]
}).compileComponents();
  }));

  it('should render one row per item', () => {
    fixture = TestBed.createComponent(PrimaryTransactionComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(1);
    expect(fixture.nativeElement.textContent).toContain('Deposit');
  });
});
