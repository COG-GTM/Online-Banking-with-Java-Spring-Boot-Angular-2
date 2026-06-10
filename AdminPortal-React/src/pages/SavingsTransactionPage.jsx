import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getSavingsTransactionList } from '../services/userService';
import { formatDate } from '../utils/formatDate';

export default function SavingsTransactionPage() {
  const { username } = useParams();
  const [savingsTransactionList, setSavingsTransactionList] = useState([]);

  useEffect(() => {
    getSavingsTransactionList(username)
      .then((res) => setSavingsTransactionList(res.data))
      .catch((error) => console.log(error));
  }, [username]);

  return (
    <div>
      <h1>Savings Account Transaction List</h1>

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
          {savingsTransactionList.map((savingsTransaction) => (
            <tr key={savingsTransaction.id}>
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
    </div>
  );
}
