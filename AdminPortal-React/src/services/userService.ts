import { API_BASE_URL } from './api';
import type { Transaction } from '../types/transaction';

// Ports AdminPortal/src/app/user.service.ts -> getSavingsTransactionList.
// Angular used Http with { withCredentials: true }; here we use fetch with
// credentials: 'include' so the session cookie is sent to the Spring Boot backend.
export async function getSavingsTransactionList(
  username: string,
  signal?: AbortSignal,
): Promise<Transaction[]> {
  const url = `${API_BASE_URL}/api/user/savings/transaction?username=${encodeURIComponent(
    username,
  )}`;

  const response = await fetch(url, {
    method: 'GET',
    credentials: 'include',
    signal,
  });

  if (!response.ok) {
    throw new Error(
      `Failed to load savings transactions (${response.status} ${response.statusText})`,
    );
  }

  return (await response.json()) as Transaction[];
}
