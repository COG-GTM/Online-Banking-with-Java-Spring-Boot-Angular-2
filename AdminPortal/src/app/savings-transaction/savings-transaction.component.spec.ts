/* tslint:disable:no-unused-variable */

import { TestBed, async } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { SavingsTransactionComponent } from './savings-transaction.component';
import { UserService } from '../user.service';

describe('Component: SavingsTransaction', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      declarations: [SavingsTransactionComponent],
      providers: [UserService]
    }).compileComponents();
  }));

  it('should create an instance', () => {
    const fixture = TestBed.createComponent(SavingsTransactionComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
