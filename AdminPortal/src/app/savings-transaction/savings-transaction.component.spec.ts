import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { UserService } from '../user.service';
import { SavingsTransactionComponent } from './savings-transaction.component';

describe('Component: SavingsTransaction', () => {
  let component: SavingsTransactionComponent;
  let fixture: ComponentFixture<SavingsTransactionComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ SavingsTransactionComponent ],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([]), UserService]
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
