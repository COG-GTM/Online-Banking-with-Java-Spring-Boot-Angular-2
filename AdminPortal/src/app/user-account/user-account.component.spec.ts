import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { inject } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { UserAccountComponent } from './user-account.component';
import { UserService } from '../user.service';

describe('UserAccountComponent', () => {
  let component: UserAccountComponent;
  let fixture: ComponentFixture<UserAccountComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      declarations: [ UserAccountComponent ],
      providers: [UserService]
    })
    .compileComponents();
  }));

  beforeEach(inject([HttpTestingController], (controller: HttpTestingController) => {
    httpMock = controller;
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserAccountComponent);
    component = fixture.componentInstance;
    httpMock.expectOne('http://localhost:8080/api/user/all').flush([]);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
