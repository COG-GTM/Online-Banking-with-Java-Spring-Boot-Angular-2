import { API_BASE_URL } from './config';

/**
 * Typed port of the Angular `LoginService`.
 *
 * - `sendCredential` -> POST `/index` with an
 *   `application/x-www-form-urlencoded` body (`username=...&password=...`).
 * - `logout` -> GET `/logout`.
 *
 * Both calls are session-cookie based, so they use `credentials: 'include'`
 * (the Angular code used `withCredentials: true`). An optional `AbortSignal`
 * lets callers cancel in-flight requests (e.g. on component unmount).
 */
export async function sendCredential(
  username: string,
  password: string,
  signal?: AbortSignal,
): Promise<Response> {
  const url = `${API_BASE_URL}/index`;
  const params = new URLSearchParams({ username, password }).toString();

  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params,
    credentials: 'include',
    signal,
  });

  if (!response.ok) {
    throw new Error(`Login failed with status ${response.status}`);
  }
  return response;
}

export async function logout(signal?: AbortSignal): Promise<Response> {
  const url = `${API_BASE_URL}/logout`;
  const response = await fetch(url, {
    method: 'GET',
    credentials: 'include',
    signal,
  });

  if (!response.ok) {
    throw new Error(`Logout failed with status ${response.status}`);
  }
  return response;
}
