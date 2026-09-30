import { Headers, RequestOptionsArgs } from '@angular/http';

export const XSRF_COOKIE_NAME = 'XSRF-TOKEN';
export const XSRF_HEADER_NAME = 'X-XSRF-TOKEN';

export function readXsrfToken(): string {
  const match = document.cookie.match(new RegExp('(?:^|;\\s*)' + XSRF_COOKIE_NAME + '=([^;]*)'));
  return match ? decodeURIComponent(match[1]) : null;
}

// The header is only added when the cookie exists: a custom header forces a CORS preflight.
export function xsrfHeaders(headers: Headers = new Headers()): Headers {
  const token = readXsrfToken();
  if (token) {
    headers.set(XSRF_HEADER_NAME, token);
  }
  return headers;
}

export function xsrfOptions(headers?: Headers): RequestOptionsArgs {
  return { headers: xsrfHeaders(headers), withCredentials: true };
}
