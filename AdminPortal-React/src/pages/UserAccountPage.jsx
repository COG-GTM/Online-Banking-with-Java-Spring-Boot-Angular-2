import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getUsers, enableUser, disableUser } from '../services/userService';

export default function UserAccountPage() {
  const [userList, setUserList] = useState([]);
  const navigate = useNavigate();

  const fetchUsers = () => {
    getUsers()
      .then((res) => setUserList(res.data))
      .catch((error) => console.log(error));
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const onSelectPrimary = (username) => {
    navigate(`/primaryTransaction/${username}`);
  };

  const onSelectSavings = (username) => {
    navigate(`/savingsTransaction/${username}`);
  };

  const handleEnable = (username) => {
    enableUser(username)
      .then(() => fetchUsers())
      .catch((error) => console.log(error));
  };

  const handleDisable = (username) => {
    disableUser(username)
      .then(() => fetchUsers())
      .catch((error) => console.log(error));
  };

  return (
    <div>
      <h1>User Account Page</h1>

      <table
        id="userTable"
        className="table table-striped"
        cellSpacing="0"
        width="100%"
      >
        <thead>
          <tr>
            <th>User Name</th>
            <th>First Name</th>
            <th>Last Name</th>
            <th>Email</th>
            <th>Phone</th>
            <th>Primary Account</th>
            <th>Savings Account</th>
            <th>Enabled</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {userList.map((user) => (
            <tr key={user.username}>
              <td>{user.username}</td>
              <td>{user.firstName}</td>
              <td>{user.lastName}</td>
              <td>{user.email}</td>
              <td>{user.phone}</td>
              <td>
                <a
                  onClick={() => onSelectPrimary(user.username)}
                  style={{ cursor: 'pointer' }}
                >
                  {user.primaryAccount && user.primaryAccount.accountBalance}
                </a>
              </td>
              <td>
                <a
                  onClick={() => onSelectSavings(user.username)}
                  style={{ cursor: 'pointer' }}
                >
                  {user.savingsAccount && user.savingsAccount.accountBalance}
                </a>
              </td>
              <td>{String(user.enabled)}</td>
              {user.enabled ? (
                <td>
                  <a
                    onClick={() => handleDisable(user.username)}
                    style={{ cursor: 'pointer' }}
                  >
                    Disable
                  </a>
                </td>
              ) : (
                <td>
                  <a
                    onClick={() => handleEnable(user.username)}
                    style={{ cursor: 'pointer' }}
                  >
                    Enable
                  </a>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
