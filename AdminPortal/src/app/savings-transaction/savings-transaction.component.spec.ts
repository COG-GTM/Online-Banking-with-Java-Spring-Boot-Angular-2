/* tslint:disable:no-unused-variable */

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SavingsTransactionComponent } from './savings-transaction.component';
import { UserService } from '../user.service';

describe('Component: SavingsTransaction', () => {
  it('should create an instance', () => {
    TestBed.configureTestingModule({
      declarations: [SavingsTransactionComponent],
      imports: [FormsModule],
      providers: [UserService, provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
    const component = TestBed.createComponent(SavingsTransactionComponent).componentInstance;
    expect(component).toBeTruthy();
  });
});
