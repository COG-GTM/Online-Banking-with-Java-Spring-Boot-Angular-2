/* tslint:disable:no-unused-variable */

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { UserService } from '../user.service';
import { SavingsTransactionComponent } from './savings-transaction.component';

describe('Component: SavingsTransaction', () => {
  it('should create an instance', () => {
    TestBed.configureTestingModule({
      providers: [SavingsTransactionComponent, UserService, provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
    let component = TestBed.inject(SavingsTransactionComponent);
    expect(component).toBeTruthy();
  });
});
