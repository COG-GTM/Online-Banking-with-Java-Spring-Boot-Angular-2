import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import type { User } from '../types/user';
import { disableUser, enableUser, getUsers } from '../services/userService';

export default function UserAccount() {
  const [userList, setUserList] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Refresh the list after a mutation (enable/disable). The button handlers run
  // outside of an effect, so updating loading state synchronously here is fine.
  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setUserList(await getUsers());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load users');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    (async () => {
      try {
        const users = await getUsers(controller.signal);
        setUserList(users);
        setError(null);
      } catch (err) {
        if (err instanceof DOMException && err.name === 'AbortError') return;
        setError(err instanceof Error ? err.message : 'Failed to load users');
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    })();
    return () => controller.abort();
  }, []);

  async function onEnable(username: string) {
    try {
      await enableUser(username);
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to enable user');
    }
  }

  async function onDisable(username: string) {
    try {
      await disableUser(username);
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to disable user');
    }
  }

  return (
    <div>
      <h1>User Account Page</h1>

      {loading && <p>Loading users...</p>}
      {error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}

      {!loading && !error && (
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
                  <Link to={`/primaryTransaction/${user.username}`}>
                    {user.primaryAccount?.accountBalance}
                  </Link>
                </td>
                <td>
                  <Link to={`/savingsTransaction/${user.username}`}>
                    {user.savingsAccount?.accountBalance}
                  </Link>
                </td>
                <td>{String(user.enabled)}</td>
                {user.enabled ? (
                  <td>
                    <a
                      onClick={() => onDisable(user.username)}
                      style={{ cursor: 'pointer' }}
                    >
                      Disable
                    </a>
                  </td>
                ) : (
                  <td>
                    <a
                      onClick={() => onEnable(user.username)}
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
      )}
    </div>
  );
}
