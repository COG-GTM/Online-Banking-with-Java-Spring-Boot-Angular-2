import { Headers } from '@angular/http';

export const XSRF_COOKIE_NAME = 'XSRF-TOKEN';
export const XSRF_HEADER_NAME = 'X-XSRF-TOKEN';

export function readXsrfToken(cookieString: string = document.cookie): string | null {
  for (const part of cookieString.split(';')) {
    const separator = part.indexOf('=');
    if (separator < 0) {
      continue;
    }
    if (part.substring(0, separator).trim() === XSRF_COOKIE_NAME) {
      try {
        return decodeURIComponent(part.substring(separator + 1).trim());
      } catch (e) {
        return null;
      }
    }
  }
  return null;
}

// Echoes the XSRF-TOKEN cookie as an X-XSRF-TOKEN header. Adds nothing when the
// cookie is absent (server CSRF disabled), so no extra CORS preflight is triggered.
export function withXsrfHeader(headers: Headers = new Headers(), cookieString?: string): Headers {
  const token = readXsrfToken(cookieString);
  if (token) {
    headers.set(XSRF_HEADER_NAME, token);
  }
  return headers;
}
