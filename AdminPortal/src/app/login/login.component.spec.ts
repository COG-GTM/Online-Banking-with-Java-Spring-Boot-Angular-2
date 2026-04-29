/* tslint:disable:no-unused-variable */

import { TestBed, async } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { LoginComponent } from './login.component';
import { LoginService } from '../login.service';

describe('Component: Login', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, FormsModule],
      declarations: [LoginComponent],
      providers: [LoginService]
    }).compileComponents();
  }));

  it('should create an instance', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
