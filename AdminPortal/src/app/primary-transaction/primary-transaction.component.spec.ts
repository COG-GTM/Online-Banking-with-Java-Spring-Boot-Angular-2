import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { PrimaryTransactionComponent } from './primary-transaction.component';

describe('PrimaryTransactionComponent', () => {
  let component: PrimaryTransactionComponent;
  let fixture: ComponentFixture<PrimaryTransactionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrimaryTransactionComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    fixture = TestBed.createComponent(PrimaryTransactionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
