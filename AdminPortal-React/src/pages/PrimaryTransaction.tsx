import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getPrimaryTransactionList } from "../services/userService";
import type { Transaction } from "../types/transaction";
import { formatDate } from "../utils/date";

/**
 * Port of the Angular `PrimaryTransactionComponent`
 * (AdminPortal/src/app/primary-transaction/primary-transaction.component.*).
 *
 * Reads `:username` from the route, fetches that user's primary-account
 * transaction history, and renders it in a Bootstrap table. Replaces the
 * RxJS subscription with `useEffect` + `AbortController` cleanup, and adds
 * explicit loading / error / empty states (the Angular version had none).
 */
export default function PrimaryTransaction() {
  const { username = "" } = useParams<{ username: string }>();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();

    async function load() {
      setLoading(true);
      setError(null);
      try {
        const data = await getPrimaryTransactionList(username, controller.signal);
        setTransactions(data);
        setLoading(false);
      } catch (err: unknown) {
        if (err instanceof DOMException && err.name === "AbortError") {
          return;
        }
        setError(err instanceof Error ? err.message : "Failed to load transactions");
        setLoading(false);
      }
    }

    void load();

    return () => controller.abort();
  }, [username]);

  return (
    <div>
      <h1>Primary Account Transaction List</h1>

      {loading && <p>Loading transactions&hellip;</p>}

      {!loading && error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}

      {!loading && !error && (
        <table className="table table-striped">
          <thead>
            <tr>
              <th>Post Date</th>
              <th>Description</th>
              <th>Type</th>
              <th>Status</th>
              <th>Amount</th>
              <th>Available Balance</th>
            </tr>
          </thead>
          <tbody>
            {transactions.length === 0 ? (
              <tr>
                <td colSpan={6}>No transactions found.</td>
              </tr>
            ) : (
              transactions.map((t) => (
                <tr key={t.id}>
                  <td>{formatDate(t.date)} </td>
                  <td>{t.description}</td>
                  <td>{t.type}</td>
                  <td>{t.status}</td>
                  <td>{t.amount}</td>
                  <td>{t.availableBalance}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      )}
    </div>
  );
}
