import { Link, useNavigate } from 'react-router-dom';
import { logout as logoutRequest } from '../services/loginService';

// Ports AdminPortal/src/app/navbar/navbar.component.*
// Nav items are hidden until the admin has logged in (mirrors getDisplay()).
export default function Navbar() {
  const navigate = useNavigate();
  const loggedIn = localStorage.getItem('PortalAdminHasLoggedIn') ? true : false;
  const itemStyle = { display: loggedIn ? '' : 'none' };

  const handleLogout = async () => {
    try {
      await logoutRequest();
    } catch {
      // ignore network errors on logout, mirror Angular console.log(err)
    } finally {
      localStorage.setItem('PortalAdminHasLoggedIn', '');
      navigate('/login');
    }
  };

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
          <Link className="navbar-brand" to="/login">
            Admin Portal
          </Link>
        </div>

        <div className="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
          <ul className="nav navbar-nav">
            <li style={itemStyle}>
              <Link to="/userAccount">
                {' '}
                User Account <span className="sr-only">(current)</span>
              </Link>
            </li>
            <li style={itemStyle}>
              <Link to="/appointment">
                {' '}
                Appointment <span className="sr-only">(current)</span>
              </Link>
            </li>
          </ul>
          <ul className="nav navbar-nav navbar-right">
            <li style={itemStyle}>
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
