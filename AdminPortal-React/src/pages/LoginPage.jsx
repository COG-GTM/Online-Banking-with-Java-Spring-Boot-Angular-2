import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { sendCredential } from '../services/loginService';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { isLoggedIn, login } = useAuth();
  const navigate = useNavigate();

  const onSubmit = (event) => {
    event.preventDefault();
    setError('');
    sendCredential(username, password)
      .then((response) => {
        // Spring Security form login answers with a 302 for both success
        // (-> /userFront) and failure (-> /index?error), and the browser
        // transparently follows it. Inspect the final URL to tell them apart.
        const finalUrl = response?.request?.responseURL || '';
        if (finalUrl.includes('error')) {
          setError('Invalid username or password.');
          return;
        }
        login();
        navigate('/userAccount');
      })
      .catch((err) => {
        console.log(err);
        setError('Unable to log in. Please try again.');
      });
  };

  return (
    <div className="wrapper">
      {!isLoggedIn ? (
        <form className="form-signin" onSubmit={onSubmit}>
          <h2 className="clean-font">Please login</h2>

          {error && (
            <div className="alert alert-danger" role="alert">
              {error}
            </div>
          )}

          <input
            type="text"
            className="form-control"
            name="username"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
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
            onChange={(event) => setPassword(event.target.value)}
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
      ) : (
        <div>
          <h2>Welcome to Admin Portal!</h2>
        </div>
      )}
    </div>
  );
}
