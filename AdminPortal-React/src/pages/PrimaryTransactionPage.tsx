import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useUsers } from '../hooks/useUsers';
import { formatDate } from '../utils/format';
import type { Transaction } from '../types';

export function PrimaryTransactionPage() {
  const { username = '' } = useParams<{ username: string }>();
  const { getPrimaryTransactions } = useUsers();
  const [transactions, setTransactions] = useState<Transaction[]>([]);

  useEffect(() => {
    getPrimaryTransactions(username)
      .then((res) => setTransactions(res.data))
      .catch((error) => console.log(error));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [username]);

  return (
    <div>
      <h1>Primary Account Transaction List</h1>

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
          {transactions.map((t) => (
            <tr key={t.id}>
              <td>{formatDate(t.date)}</td>
              <td>{t.description}</td>
              <td>{t.type}</td>
              <td>{t.status}</td>
              <td>{t.amount}</td>
              <td>{t.availableBalance}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
