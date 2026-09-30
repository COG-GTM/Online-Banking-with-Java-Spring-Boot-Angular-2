import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { UserAccountComponent } from './user-account.component';

describe('UserAccountComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [UserAccountComponent],
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('renders the users returned by the API and refreshes after disabling', async () => {
    const fixture = TestBed.createComponent(UserAccountComponent);
    const user = {
      username: 'jdoe',
      firstName: 'John',
      lastName: 'Doe',
      email: 'jdoe@example.com',
      phone: '555-0100',
      enabled: true,
      primaryAccount: { accountBalance: 100 },
      savingsAccount: { accountBalance: 200 },
    };
    httpMock.expectOne('http://localhost:8080/api/user/all').flush([user]);
    await fixture.whenStable();

    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(1);
    expect(rows[0].textContent).toContain('jdoe@example.com');

    const disable = Array.from<HTMLAnchorElement>(rows[0].querySelectorAll('a')).find(
      (a) => a.textContent === 'Disable',
    );
    disable?.click();
    httpMock.expectOne('http://localhost:8080/api/user/jdoe/disable').flush(null);
    httpMock.expectOne('http://localhost:8080/api/user/all').flush([{ ...user, enabled: false }]);
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('tbody tr').textContent).toContain('false');
  });
});
