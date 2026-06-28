import { API_BASE_URL } from '../config';
import type { User } from '../types/user';

// All Admin Portal API calls are session-cookie based. The Angular code used
// `withCredentials: true`; the fetch equivalent is `credentials: 'include'`.
const CREDENTIALS: RequestCredentials = 'include';

async function ensureOk(res: Response): Promise<Response> {
  if (!res.ok) {
    throw new Error(`Request failed with status ${res.status}`);
  }
  return res;
}

// GET /api/user/all
export async function getUsers(signal?: AbortSignal): Promise<User[]> {
  const res = await fetch(`${API_BASE_URL}/api/user/all`, {
    credentials: CREDENTIALS,
    signal,
  });
  await ensureOk(res);
  return (await res.json()) as User[];
}

// GET /api/user/{username}/enable
export async function enableUser(
  username: string,
  signal?: AbortSignal,
): Promise<void> {
  const res = await fetch(
    `${API_BASE_URL}/api/user/${encodeURIComponent(username)}/enable`,
    { credentials: CREDENTIALS, signal },
  );
  await ensureOk(res);
}

// GET /api/user/{username}/disable
export async function disableUser(
  username: string,
  signal?: AbortSignal,
): Promise<void> {
  const res = await fetch(
    `${API_BASE_URL}/api/user/${encodeURIComponent(username)}/disable`,
    { credentials: CREDENTIALS, signal },
  );
  await ensureOk(res);
}
