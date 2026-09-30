/* tslint:disable:no-unused-variable */

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { PrimaryTransactionComponent } from './primary-transaction.component';
import { UserService } from '../user.service';

describe('Component: PrimaryTransaction', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PrimaryTransactionComponent, UserService, provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
  });

  it('should create an instance', () => {
    let component = TestBed.inject(PrimaryTransactionComponent);
    expect(component).toBeTruthy();
  });
});
