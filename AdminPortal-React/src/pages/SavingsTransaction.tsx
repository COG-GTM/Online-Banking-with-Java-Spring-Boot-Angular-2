import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import type { Transaction } from '../types/transaction';
import { getSavingsTransactionList } from '../services/userService';
import { formatDate } from '../utils/format';

// Ports AdminPortal/src/app/savings-transaction/savings-transaction.component.*
export default function SavingsTransaction() {
  const { username = '' } = useParams<{ username: string }>();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();

    setLoading(true);
    setError(null);

    getSavingsTransactionList(username, controller.signal)
      .then((list) => {
        setTransactions(list);
        setLoading(false);
      })
      .catch((err: unknown) => {
        if (err instanceof DOMException && err.name === 'AbortError') {
          return;
        }
        setError(err instanceof Error ? err.message : 'Failed to load transactions');
        setLoading(false);
      });

    return () => controller.abort();
  }, [username]);

  return (
    <div>
      <h1>Savings Account Transaction List</h1>

      {loading && <p>Loading transactions...</p>}

      {!loading && error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}

      {!loading && !error && transactions.length === 0 && (
        <p>No savings transactions found.</p>
      )}

      {!loading && !error && transactions.length > 0 && (
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
            {transactions.map((savingsTransaction, index) => (
              <tr key={savingsTransaction.id ?? index}>
                <td>{formatDate(savingsTransaction.date)} </td>
                <td>{savingsTransaction.description}</td>
                <td>{savingsTransaction.type}</td>
                <td>{savingsTransaction.status}</td>
                <td>{savingsTransaction.amount}</td>
                <td>{savingsTransaction.availableBalance}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
