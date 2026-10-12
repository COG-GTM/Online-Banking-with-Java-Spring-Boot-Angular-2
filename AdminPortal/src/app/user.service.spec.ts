import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { UserService } from './user.service';

describe('Service: User', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [UserService, provideHttpClient(), provideHttpClientTesting()]
    });
  });

  it('should ...', () => {
    expect(TestBed.inject(UserService)).toBeTruthy();
  });
});
