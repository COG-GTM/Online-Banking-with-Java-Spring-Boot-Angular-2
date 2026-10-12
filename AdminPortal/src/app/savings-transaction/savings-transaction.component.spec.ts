import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { SavingsTransactionComponent } from './savings-transaction.component';
import { UserService } from '../user.service';

describe('SavingsTransactionComponent', () => {
  let component: SavingsTransactionComponent;
  let fixture: ComponentFixture<SavingsTransactionComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SavingsTransactionComponent],
      imports: [HttpClientTestingModule],
      providers: [
        UserService,
        { provide: ActivatedRoute, useValue: { params: of({ username: 'john' }) } }
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(SavingsTransactionComponent);
    component = fixture.componentInstance;
  });

  it('should load the savings transactions of the routed user', () => {
    const transactions = [{ transactionId: 2 }];
    httpMock
      .expectOne('http://localhost:8080/api/user/savings/transaction?username=john')
      .flush(transactions);

    expect(component.username).toBe('john');
    expect(component.savingsTransactionList).toEqual(transactions);
  });
});
