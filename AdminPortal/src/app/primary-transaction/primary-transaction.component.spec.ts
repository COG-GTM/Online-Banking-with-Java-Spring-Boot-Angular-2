/* tslint:disable:no-unused-variable */

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { PrimaryTransactionComponent } from './primary-transaction.component';
import { UserService } from '../user.service';

describe('Component: PrimaryTransaction', () => {
  it('should create an instance', () => {
    TestBed.configureTestingModule({
      declarations: [PrimaryTransactionComponent],
      providers: [UserService, provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
    let component = TestBed.createComponent(PrimaryTransactionComponent).componentInstance;
    expect(component).toBeTruthy();
  });
});
