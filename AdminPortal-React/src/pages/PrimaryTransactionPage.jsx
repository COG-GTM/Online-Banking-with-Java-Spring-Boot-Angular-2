import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getPrimaryTransactionList } from '../services/userService';
import { formatDate } from '../utils/formatDate';

export default function PrimaryTransactionPage() {
  const { username } = useParams();
  const [primaryTransactionList, setPrimaryTransactionList] = useState([]);

  useEffect(() => {
    getPrimaryTransactionList(username)
      .then((res) => setPrimaryTransactionList(res.data))
      .catch((error) => console.log(error));
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
          {primaryTransactionList.map((primaryTransaction) => (
            <tr key={primaryTransaction.id}>
              <td>{formatDate(primaryTransaction.date)} </td>
              <td>{primaryTransaction.description}</td>
              <td>{primaryTransaction.type}</td>
              <td>{primaryTransaction.status}</td>
              <td>{primaryTransaction.amount}</td>
              <td>{primaryTransaction.availableBalance}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
