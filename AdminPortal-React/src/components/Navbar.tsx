import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export function Navbar() {
  const { loggedIn, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await logout();
    } catch (err) {
      console.log(err);
    }
    navigate('/login');
  };

  const hidden = (visible: boolean) => ({ display: visible ? '' : 'none' });

  return (
    <nav className="navbar navbar-clean">
      <div className="container-fluid">
        {/* Brand and toggle get grouped for better mobile display */}
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

        {/* Collect the nav links, forms, and other content for toggling */}
        <div
          className="collapse navbar-collapse"
          id="bs-example-navbar-collapse-1"
        >
          <ul className="nav navbar-nav">
            <li style={hidden(loggedIn)}>
              <NavLink to="/userAccount">
                {' '}
                User Account <span className="sr-only">(current)</span>
              </NavLink>
            </li>
            <li style={hidden(loggedIn)}>
              <NavLink to="/appointment">
                {' '}
                Appointment <span className="sr-only">(current)</span>
              </NavLink>
            </li>
          </ul>
          <ul className="nav navbar-nav navbar-right">
            <li style={hidden(loggedIn)}>
              <a onClick={handleLogout} style={{ cursor: 'pointer' }}>
                Logout
              </a>
            </li>
          </ul>
        </div>
        {/* /.navbar-collapse */}
      </div>
      {/* /.container-fluid */}
    </nav>
  );
}
