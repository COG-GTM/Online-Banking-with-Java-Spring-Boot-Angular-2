import { useState, type FormEvent } from 'react';
import { useAuth } from '../hooks/useAuth';
import { useAuthContext } from '../context/authContext';

export function LoginPage() {
  const { login } = useAuth();
  const { authValue, setLoggedIn } = useAuthContext();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  // Mirrors AdminPortal/src/app/login/login.component.ts: the login view treats a
  // missing/empty key as logged-out (show the form) and any stored value as
  // logged-in (show the welcome message).
  const loggedIn = !(authValue === '' || authValue === null);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    try {
      await login(username, password);
      setLoggedIn(true);
    } catch (err) {
      console.log(err);
    }
  };

  return (
    <div className="wrapper">
      <form className="form-signin" onSubmit={onSubmit} hidden={loggedIn}>
        <h2 className="clean-font">Please login</h2>

        <input
          type="text"
          className="form-control"
          name="username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          placeholder="Username"
          required
          autoFocus
        />
        <br />

        <input
          type="password"
          className="form-control"
          name="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Password"
          required
        />
        <div className="form-group">
          <br />
          <label>
            <input type="checkbox" name="remember-me" id="remember-me" />
            &nbsp;<span className="clean-font">Remember me</span>
          </label>
        </div>

        <button className="btn btn-primary btn-block" type="submit">
          Login
        </button>
      </form>
      <div hidden={!loggedIn}>
        <h2>Welcome to Admin Portal!</h2>
      </div>
    </div>
  );
}
