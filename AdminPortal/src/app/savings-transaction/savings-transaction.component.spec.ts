import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { SavingsTransactionComponent } from './savings-transaction.component';

describe('SavingsTransactionComponent', () => {
  let component: SavingsTransactionComponent;
  let fixture: ComponentFixture<SavingsTransactionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SavingsTransactionComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    fixture = TestBed.createComponent(SavingsTransactionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
