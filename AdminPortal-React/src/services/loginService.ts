import { API_BASE_URL } from './api';

// Minimal port of AdminPortal/src/app/login.service.ts -> logout, needed by the
// shared navbar shell. Full login flow is owned by another migration slice.
export async function logout(): Promise<void> {
  await fetch(`${API_BASE_URL}/logout`, {
    method: 'GET',
    credentials: 'include',
  });
}
