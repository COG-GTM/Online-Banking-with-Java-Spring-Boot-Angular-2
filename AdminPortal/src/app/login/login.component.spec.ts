import { ComponentFixture, TestBed, async } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import 'rxjs/add/observable/of';
import 'rxjs/add/observable/throw';

import { LoginComponent } from './login.component';
import { LoginService } from '../login.service';

class MockLoginService {
  sendCredential(username: string, password: string) {
    return Observable.of({});
  }
}

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let loginService: MockLoginService;

  beforeEach(async(() => {
    localStorage.removeItem('PortalAdminHasLoggedIn');
    TestBed.configureTestingModule({
      imports: [FormsModule],
      declarations: [LoginComponent],
      providers: [
        { provide: LoginService, useClass: MockLoginService }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    loginService = TestBed.get(LoginService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
    expect(component.loggedIn).toBe(false);
  });

  it('onSubmit() should call LoginService.sendCredential with the entered credentials', () => {
    // Return a stream that never emits so the success handler (which calls the
    // non-stubbable global location.reload()) does not run during the test.
    const sendCredential = spyOn(loginService, 'sendCredential').and.returnValue(new Subject());

    component.username = 'admin';
    component.password = 'secret';
    component.onSubmit();

    expect(sendCredential).toHaveBeenCalledWith('admin', 'secret');
  });

  it('should not mark the user logged in when login fails', () => {
    spyOn(loginService, 'sendCredential').and.returnValue(Observable.throw('bad credentials'));

    component.username = 'admin';
    component.password = 'wrong';
    component.onSubmit();

    expect(loginService.sendCredential).toHaveBeenCalled();
    expect(component.loggedIn).toBe(false);
  });
});
