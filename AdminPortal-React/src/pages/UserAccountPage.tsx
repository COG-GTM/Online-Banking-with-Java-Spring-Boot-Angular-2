import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUsers } from '../hooks/useUsers';
import type { User } from '../types';

export function UserAccountPage() {
  const { getUsers, enableUser, disableUser } = useUsers();
  const navigate = useNavigate();
  const [userList, setUserList] = useState<User[]>([]);

  const loadUsers = useCallback(() => {
    getUsers()
      .then((res) => setUserList(res.data))
      .catch((error) => console.log(error));
  }, [getUsers]);

  useEffect(() => {
    loadUsers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleEnable = async (username: string) => {
    try {
      await enableUser(username);
    } catch (error) {
      console.log(error);
    }
    loadUsers();
  };

  const handleDisable = async (username: string) => {
    try {
      await disableUser(username);
    } catch (error) {
      console.log(error);
    }
    loadUsers();
  };

  return (
    <div>
      <h1>User Account Page</h1>

      <table
        id="userTable"
        className="table table-striped"
        cellSpacing={0}
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
                  onClick={() =>
                    navigate(`/primaryTransaction/${user.username}`)
                  }
                  style={{ cursor: 'pointer' }}
                >
                  {user.primaryAccount?.accountBalance}
                </a>
              </td>
              <td>
                <a
                  onClick={() =>
                    navigate(`/savingsTransaction/${user.username}`)
                  }
                  style={{ cursor: 'pointer' }}
                >
                  {user.savingsAccount?.accountBalance}
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
