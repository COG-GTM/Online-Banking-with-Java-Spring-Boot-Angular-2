/* tslint:disable:no-unused-variable */

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { PrimaryTransactionComponent } from './primary-transaction.component';

describe('Component: PrimaryTransaction', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PrimaryTransactionComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
      schemas: [NO_ERRORS_SCHEMA]
    });
  });

  it('should create an instance', () => {
    let component = TestBed.createComponent(PrimaryTransactionComponent).componentInstance;
    expect(component).toBeTruthy();
  });
});
