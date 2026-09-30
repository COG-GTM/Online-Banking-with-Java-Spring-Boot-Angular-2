/* tslint:disable:no-unused-variable */

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { SavingsTransactionComponent } from './savings-transaction.component';
import { UserService } from '../user.service';

describe('Component: SavingsTransaction', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SavingsTransactionComponent, UserService, provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
  });

  it('should create an instance', () => {
    let component = TestBed.inject(SavingsTransactionComponent);
    expect(component).toBeTruthy();
  });
});
