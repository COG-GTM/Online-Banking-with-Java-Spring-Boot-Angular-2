import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { PrimaryTransactionComponent } from './primary-transaction.component';
import { UserService } from '../user.service';

describe('PrimaryTransactionComponent', () => {
  let component: PrimaryTransactionComponent;
  let fixture: ComponentFixture<PrimaryTransactionComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PrimaryTransactionComponent],
      imports: [HttpClientTestingModule],
      providers: [
        UserService,
        { provide: ActivatedRoute, useValue: { params: of({ username: 'john' }) } }
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(PrimaryTransactionComponent);
    component = fixture.componentInstance;
  });

  it('should load the transactions of the routed user', () => {
    const transactions = [{ transactionId: 1 }];
    httpMock
      .expectOne('http://localhost:8080/api/user/primary/transaction?username=john')
      .flush(transactions);

    expect(component.username).toBe('john');
    expect(component.primaryTransactionList).toEqual(transactions);
  });
});
