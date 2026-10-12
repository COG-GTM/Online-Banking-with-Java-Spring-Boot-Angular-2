import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { provideRouter } from '@angular/router';

import { PrimaryTransactionComponent } from './primary-transaction.component';
import { UserService } from '../user.service';

describe('Component: PrimaryTransaction', () => {
  let component: PrimaryTransactionComponent;
  let fixture: ComponentFixture<PrimaryTransactionComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PrimaryTransactionComponent ],
      imports: [ FormsModule ],
      providers: [ UserService, provideHttpClient(), provideHttpClientTesting(), provideRouter([]) ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PrimaryTransactionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create an instance', () => {
    expect(component).toBeTruthy();
  });
});
