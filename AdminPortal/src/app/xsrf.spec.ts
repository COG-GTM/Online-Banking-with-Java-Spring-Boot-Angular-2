import { Headers } from '@angular/http';
import { readXsrfToken, xsrfHeaders, xsrfOptions, XSRF_HEADER_NAME } from './xsrf';

describe('xsrf helper', () => {
  it('reads and decodes the XSRF-TOKEN cookie', () => {
    expect(readXsrfToken('JSESSIONID=abc; XSRF-TOKEN=a%2Bb%3D; other=1')).toBe('a+b=');
  });

  it('returns the raw value when the cookie has a malformed escape', () => {
    expect(readXsrfToken('XSRF-TOKEN=bad%')).toBe('bad%');
  });

  it('returns null when the cookie is absent', () => {
    expect(readXsrfToken('JSESSIONID=abc; NOT-XSRF-TOKEN=x')).toBeNull();
    expect(readXsrfToken('')).toBeNull();
  });

  it('adds X-XSRF-TOKEN when the cookie is present', () => {
    const headers = xsrfHeaders(new Headers({ 'Content-Type': 'text/plain' }), 'XSRF-TOKEN=tok');
    expect(headers.get(XSRF_HEADER_NAME)).toBe('tok');
    expect(headers.get('Content-Type')).toBe('text/plain');
  });

  it('adds no header when the cookie is absent', () => {
    const headers = xsrfHeaders(new Headers(), 'JSESSIONID=abc');
    expect(headers.has(XSRF_HEADER_NAME)).toBe(false);
  });

  it('always sets withCredentials', () => {
    expect(xsrfOptions(undefined, '').withCredentials).toBe(true);
    expect(xsrfOptions(undefined, 'XSRF-TOKEN=tok').withCredentials).toBe(true);
  });
});
