import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { sendCredential } from '../services/loginService';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const { isLoggedIn, login } = useAuth();
  const navigate = useNavigate();

  const onSubmit = (event) => {
    event.preventDefault();
    sendCredential(username, password)
      .then(() => {
        login();
        navigate('/userAccount');
      })
      .catch((err) => console.log(err));
  };

  return (
    <div className="wrapper">
      {!isLoggedIn ? (
        <form className="form-signin" onSubmit={onSubmit}>
          <h2 className="clean-font">Please login</h2>

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
