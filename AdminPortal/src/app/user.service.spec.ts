/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { UserService } from './user.service';

describe('Service: User', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [UserService]
    });
  });

  it('should ...', inject([UserService], (service: UserService) => {
    expect(service).toBeTruthy();
  }));
});

describe('UserService.usernamePathSegment', () => {
  it('encodes characters that would rewrite the admin API path', () => {
    expect(UserService.usernamePathSegment('alice/disable?x=')).toBe('alice%2Fdisable%3Fx%3D');
    expect(UserService.usernamePathSegment('../appointment/7/confirm?x=')).toBe('..%2Fappointment%2F7%2Fconfirm%3Fx%3D');
    expect(UserService.usernamePathSegment('bob#x')).toBe('bob%23x');
  });

  it('leaves ordinary usernames unchanged', () => {
    expect(UserService.usernamePathSegment('john.doe_99-x')).toBe('john.doe_99-x');
  });

  it('rejects dot segments and empty usernames', () => {
    expect(() => UserService.usernamePathSegment('.')).toThrow();
    expect(() => UserService.usernamePathSegment('..')).toThrow();
    expect(() => UserService.usernamePathSegment('')).toThrow();
  });
});
