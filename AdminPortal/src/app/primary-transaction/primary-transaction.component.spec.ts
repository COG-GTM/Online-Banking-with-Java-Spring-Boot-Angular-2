/* tslint:disable:no-unused-variable */

import { TestBed, async } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { PrimaryTransactionComponent } from './primary-transaction.component';
import { UserService } from '../user.service';

describe('Component: PrimaryTransaction', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      declarations: [PrimaryTransactionComponent],
      providers: [UserService]
    }).compileComponents();
  }));

  it('should create an instance', () => {
    const fixture = TestBed.createComponent(PrimaryTransactionComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
