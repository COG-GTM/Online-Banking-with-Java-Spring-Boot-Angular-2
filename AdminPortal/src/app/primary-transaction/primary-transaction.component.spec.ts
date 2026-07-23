/* tslint:disable:no-unused-variable */

import { TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { HttpModule, XHRBackend } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';

import { PrimaryTransactionComponent } from './primary-transaction.component';
import { UserService } from '../user.service';

describe('Component: PrimaryTransaction', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpModule],
      declarations: [PrimaryTransactionComponent],
      providers: [
        UserService,
        { provide: XHRBackend, useClass: MockBackend },
        { provide: ActivatedRoute, useValue: { params: Observable.of({ username: 'alice' }) } }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    });
  });

  it('should create an instance', () => {
    const fixture = TestBed.createComponent(PrimaryTransactionComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should read the username from the activated route', () => {
    const fixture = TestBed.createComponent(PrimaryTransactionComponent);
    expect(fixture.componentInstance.username).toBe('alice');
  });
});
