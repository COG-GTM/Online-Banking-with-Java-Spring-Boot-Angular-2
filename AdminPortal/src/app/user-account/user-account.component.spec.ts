import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';

import { UserAccountComponent } from './user-account.component';
import { UserService } from '../user.service';

describe('UserAccountComponent', () => {
  let component: UserAccountComponent;
  let fixture: ComponentFixture<UserAccountComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [UserAccountComponent],
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [UserService]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(UserAccountComponent);
    component = fixture.componentInstance;
  });

  it('should load the user list on creation', () => {
    const users = [{ username: 'john' }];
    httpMock.expectOne('http://localhost:8080/api/user/all').flush(users);

    expect(component).toBeTruthy();
    expect(component.userList).toEqual(users);
  });

  it('should navigate to the primary transaction view for a user', () => {
    httpMock.expectOne('http://localhost:8080/api/user/all').flush([]);
    const router = TestBed.inject(Router);
    const navigate = spyOn(router, 'navigate');

    component.onSelectPrimary('john');

    expect(navigate).toHaveBeenCalledWith(['/primaryTransaction', 'john']);
  });
});
