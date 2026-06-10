import { NavLink, useNavigate } from 'react-router-dom';
import { logout as logoutRequest } from '../services/loginService';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { isLoggedIn, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logoutRequest()
      .catch((error) => console.log(error))
      .finally(() => {
        logout();
        navigate('/login');
      });
  };

  const display = isLoggedIn ? '' : 'none';

  return (
    <nav className="navbar navbar-clean">
      <div className="container-fluid">
        <div className="navbar-header">
          <button
            type="button"
            className="navbar-toggle collapsed"
            data-toggle="collapse"
            data-target="#bs-example-navbar-collapse-1"
          >
            <span className="sr-only">Toggle navigation</span>
            <span className="icon-bar"></span>
            <span className="icon-bar"></span>
            <span className="icon-bar"></span>
          </button>
          <NavLink className="navbar-brand" to="/login">
            Admin Portal
          </NavLink>
        </div>

        <div
          className="collapse navbar-collapse"
          id="bs-example-navbar-collapse-1"
        >
          <ul className="nav navbar-nav">
            <li style={{ display }}>
              <NavLink to="/userAccount">
                {' '}
                User Account <span className="sr-only">(current)</span>
              </NavLink>
            </li>
            <li style={{ display }}>
              <NavLink to="/appointment">
                {' '}
                Appointment <span className="sr-only">(current)</span>
              </NavLink>
            </li>
          </ul>
          <ul className="nav navbar-nav navbar-right">
            <li style={{ display }}>
              <a onClick={handleLogout} style={{ cursor: 'pointer' }}>
                Logout
              </a>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  );
}
