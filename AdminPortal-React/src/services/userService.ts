import { API_BASE_URL } from "./api";
import type { Transaction } from "../types/transaction";

/**
 * React/fetch port of the Angular `UserService` (AdminPortal/src/app/user.service.ts).
 * Replaces RxJS `Http` + `withCredentials: true` with `fetch` + `credentials: "include"`.
 */

/**
 * GET /api/user/primary/transaction?username=...
 *
 * Returns the primary-account transaction history for the given user.
 * Accepts an optional `AbortSignal` so callers can cancel the request on
 * component unmount (the Angular code relied on Observable unsubscription).
 */
export async function getPrimaryTransactionList(
  username: string,
  signal?: AbortSignal,
): Promise<Transaction[]> {
  const url = `${API_BASE_URL}/api/user/primary/transaction?username=${encodeURIComponent(
    username,
  )}`;

  const res = await fetch(url, {
    method: "GET",
    credentials: "include",
    signal,
  });

  if (!res.ok) {
    throw new Error(`Failed to load transactions (HTTP ${res.status})`);
  }

  return (await res.json()) as Transaction[];
}
