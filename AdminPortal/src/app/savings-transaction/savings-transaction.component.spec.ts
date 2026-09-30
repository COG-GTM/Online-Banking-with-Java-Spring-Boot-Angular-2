import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { provideRouter } from '@angular/router';

import { SavingsTransactionComponent } from './savings-transaction.component';
import { UserService } from '../user.service';

describe('Component: SavingsTransaction', () => {
  let component: SavingsTransactionComponent;
  let fixture: ComponentFixture<SavingsTransactionComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ SavingsTransactionComponent ],
      imports: [ FormsModule ],
      providers: [ UserService, provideHttpClient(), provideHttpClientTesting(), provideRouter([]) ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SavingsTransactionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create an instance', () => {
    expect(component).toBeTruthy();
  });
});
