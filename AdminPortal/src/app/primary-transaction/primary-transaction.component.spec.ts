import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';

import { PrimaryTransactionComponent } from './primary-transaction.component';
import { UserService } from '../user.service';

describe('PrimaryTransactionComponent', () => {
  let fixture: ComponentFixture<PrimaryTransactionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [PrimaryTransactionComponent],
      providers: [{ provide: UserService, useValue: { getPrimaryTransactionList: () => Observable.of({ _body: JSON.stringify([{ date: 0, description: "Deposit", type: "Account", status: "Finished", amount: 100, availableBalance: 100 }]) }) } }],
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
