import { useEffect, useRef, useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export function Login() {
  const { loggedIn, login } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);

  // Cancel an in-flight login request if the component unmounts.
  const controllerRef = useRef<AbortController | null>(null);
  useEffect(() => () => controllerRef.current?.abort(), []);

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);

    controllerRef.current?.abort();
    const controller = new AbortController();
    controllerRef.current = controller;

    try {
      await login(username, password, controller.signal);
      navigate('/userAccount');
    } catch (err) {
      if (err instanceof DOMException && err.name === 'AbortError') {
        return;
      }
      console.log(err);
      setError('Invalid username or password. Please try again.');
    }
  };

  return (
    <div className="wrapper">
      {!loggedIn ? (
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

          {error && (
            <div className="alert alert-danger" role="alert">
              {error}
            </div>
          )}

          <button className="btn btn-primary btn-block" type="submit">
            Login
          </button>
        </form>
      ) : (
        <div>
          <h2>Welcome to Admin Portal!</h2>
        </div>
      )}
    </div>
  );
}
