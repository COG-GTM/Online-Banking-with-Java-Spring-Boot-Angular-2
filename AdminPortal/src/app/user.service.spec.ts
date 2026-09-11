/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { UserService } from './user.service';

describe('Service: User', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UserService]
    });
  });

  beforeEach(inject([HttpTestingController], (controller: HttpTestingController) => {
    httpMock = controller;
  }));

  afterEach(() => {
    httpMock.verify();
  });

  it('should ...', inject([UserService], (service: UserService) => {
    expect(service).toBeTruthy();
  }));

  it('should get users', inject([UserService], (service: UserService) => {
    service.getUsers().subscribe(users => expect(users).toEqual([]));

    httpMock.expectOne(req =>
      req.url === 'http://localhost:8080/api/user/all' && req.withCredentials === true
    ).flush([]);
  }));
});
