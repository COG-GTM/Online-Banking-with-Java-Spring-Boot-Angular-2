import { Headers } from '@angular/http';
import { readXsrfToken, withXsrfHeader, XSRF_HEADER_NAME } from './xsrf';

describe('xsrf', () => {
  it('reads the XSRF-TOKEN cookie among others', () => {
    expect(readXsrfToken('JSESSIONID=abc; XSRF-TOKEN=tok%3D1; other=x')).toBe('tok=1');
  });

  it('returns null when the cookie is absent', () => {
    expect(readXsrfToken('JSESSIONID=abc; MY-XSRF-TOKEN=nope')).toBeNull();
    expect(readXsrfToken('')).toBeNull();
  });

  it('sets X-XSRF-TOKEN when the cookie is present', () => {
    const headers = withXsrfHeader(new Headers({'Content-Type': 'application/x-www-form-urlencoded'}), 'XSRF-TOKEN=t1');
    expect(headers.get(XSRF_HEADER_NAME)).toBe('t1');
    expect(headers.get('Content-Type')).toBe('application/x-www-form-urlencoded');
  });

  it('leaves headers untouched when the cookie is absent', () => {
    expect(withXsrfHeader(new Headers(), 'JSESSIONID=abc').has(XSRF_HEADER_NAME)).toBe(false);
  });
});
