import { Headers, RequestOptionsArgs } from '@angular/http';

export const XSRF_COOKIE_NAME = 'XSRF-TOKEN';
export const XSRF_HEADER_NAME = 'X-XSRF-TOKEN';

export function readXsrfToken(cookieString: string = document.cookie): string {
  const prefix = XSRF_COOKIE_NAME + '=';
  for (const cookie of cookieString.split(';')) {
    const trimmed = cookie.trim();
    if (trimmed.indexOf(prefix) === 0) {
      return decodeURIComponent(trimmed.substring(prefix.length));
    }
  }
  return null;
}

// Adds X-XSRF-TOKEN only when the cookie exists: a custom header forces a CORS preflight,
// which the Boot 1.5.4 RequestFilter does not allow for x-xsrf-token.
export function xsrfHeaders(headers: Headers = new Headers(), cookieString?: string): Headers {
  const token = readXsrfToken(cookieString);
  if (token) {
    headers.set(XSRF_HEADER_NAME, token);
  }
  return headers;
}

export function xsrfOptions(headers?: Headers, cookieString?: string): RequestOptionsArgs {
  return { headers: xsrfHeaders(headers, cookieString), withCredentials: true };
}
