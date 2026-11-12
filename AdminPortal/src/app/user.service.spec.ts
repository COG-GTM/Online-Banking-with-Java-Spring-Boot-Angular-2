import { TestBed, inject } from '@angular/core/testing';
import { BaseRequestOptions, Http, RequestMethod } from '@angular/http';
import { MockBackend, MockConnection } from '@angular/http/testing';
import { UserService } from './user.service';

describe('Service: User', () => {
  let connections: MockConnection[];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        UserService,
        MockBackend,
        BaseRequestOptions,
        {
          provide: Http,
          useFactory: (b: MockBackend, options: BaseRequestOptions) => new Http(b, options),
          deps: [MockBackend, BaseRequestOptions]
        }
      ]
    });
    connections = [];
    TestBed.get(MockBackend).connections.subscribe((c: MockConnection) => connections.push(c));
  });

  function expectCredentialedPost(url: string) {
    expect(connections.length).toBe(1);
    expect(connections[0].request.method).toBe(RequestMethod.Post);
    expect(connections[0].request.url).toBe(url);
    expect(connections[0].request.withCredentials).toBe(true);
  }

  it('enables a user with POST', inject([UserService], (service: UserService) => {
    service.enableUser('alice').subscribe();
    expectCredentialedPost('http://localhost:8080/api/user/alice/enable');
  }));

  it('disables a user with POST', inject([UserService], (service: UserService) => {
    service.disableUser('alice').subscribe();
    expectCredentialedPost('http://localhost:8080/api/user/alice/disable');
  }));
});
