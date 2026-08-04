import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';

import { LoginComponent } from './login.component';
import { LoginService } from '../login.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    localStorage.removeItem('PortalAdminHasLoggedIn');

    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [FormsModule, HttpClientTestingModule],
      providers: [LoginService]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    localStorage.removeItem('PortalAdminHasLoggedIn');
  });

  it('should be created and start logged out', () => {
    expect(component).toBeTruthy();
    expect(component.loggedIn).toBe(false);
  });

  it('should post the credentials on submit', () => {
    component.username = 'admin';
    component.password = 'secret';

    component.onSubmit();

    const req = httpMock.expectOne('http://localhost:8080/index');
    expect(req.request.body).toBe('username=admin&password=secret');
  });
});
