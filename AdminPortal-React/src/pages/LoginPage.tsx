import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useAuthContext } from '../context/authContext';

export function LoginPage() {
  const { login } = useAuth();
  const { isLoggedIn, setLoggedIn } = useAuthContext();
  const navigate = useNavigate();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      await login(username, password);
      setLoggedIn(true);
      navigate('/userAccount');
    } catch (err) {
      console.log(err);
      setError('Login failed. Please check your credentials.');
    }
  };

  if (isLoggedIn) {
    return (
      <div className="wrapper">
        <h2>Welcome to Admin Portal!</h2>
      </div>
    );
  }

  return (
    <div className="wrapper">
      <form className="form-signin" onSubmit={onSubmit}>
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

        {error && <div className="alert alert-danger">{error}</div>}

        <button className="btn btn-primary btn-block w-100" type="submit">
          Login
        </button>
      </form>
    </div>
  );
}
