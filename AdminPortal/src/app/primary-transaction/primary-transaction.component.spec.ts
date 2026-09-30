/* tslint:disable:no-unused-variable */

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { UserService } from '../user.service';
import { PrimaryTransactionComponent } from './primary-transaction.component';

describe('Component: PrimaryTransaction', () => {
  it('should create an instance', () => {
    TestBed.configureTestingModule({
      providers: [PrimaryTransactionComponent, UserService, provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
    let component = TestBed.inject(PrimaryTransactionComponent);
    expect(component).toBeTruthy();
  });
});
