/* tslint:disable:no-unused-variable */

import { TestBed, async, ComponentFixture, inject } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import { PrimaryTransactionComponent } from './primary-transaction.component';
import { UserService } from '../user.service';

describe('Component: PrimaryTransaction', () => {
  let component: PrimaryTransactionComponent;
  let fixture: ComponentFixture<PrimaryTransactionComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [
        UserService,
        { provide: ActivatedRoute, useValue: { params: Observable.of({ username: 'testuser' }) } }
      ],
      declarations: [PrimaryTransactionComponent]
    }).compileComponents();
  }));

  beforeEach(inject([HttpTestingController], (controller: HttpTestingController) => {
    httpMock = controller;
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PrimaryTransactionComponent);
    component = fixture.componentInstance;
    httpMock.expectOne('http://localhost:8080/api/user/primary/transaction?username=testuser').flush([]);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create an instance', () => {
    expect(component).toBeTruthy();
    expect(component.username).toBe('testuser');
  });
});
