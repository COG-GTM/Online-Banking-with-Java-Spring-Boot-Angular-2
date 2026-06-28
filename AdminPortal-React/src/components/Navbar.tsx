import { NavLink, useNavigate } from 'react-router-dom';
import { API_BASE_URL } from '../config';

/**
 * App shell navbar, ported from the Angular `navbar.component`.
 *
 * In the full app the nav links are hidden until an admin logs in (the Angular
 * code gated them on `localStorage['PortalAdminHasLoggedIn']`). Login is owned
 * by a separate migration slice, so this scaffold always shows the links it is
 * responsible for (the User Account page) to keep the page reviewable on its own.
 */
export default function Navbar() {
  const navigate = useNavigate();

  async function logout() {
    try {
      await fetch(`${API_BASE_URL}/logout`, { credentials: 'include' });
    } catch {
      // ignore network errors on logout
    }
    localStorage.setItem('PortalAdminHasLoggedIn', '');
    navigate('/userAccount');
  }

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
          <NavLink className="navbar-brand" to="/userAccount">
            Admin Portal
          </NavLink>
        </div>

        <div
          className="collapse navbar-collapse"
          id="bs-example-navbar-collapse-1"
        >
          <ul className="nav navbar-nav">
            <li>
              <NavLink to="/userAccount">
                {' '}
                User Account <span className="sr-only">(current)</span>
              </NavLink>
            </li>
          </ul>
          <ul className="nav navbar-nav navbar-right">
            <li>
              <a onClick={logout} style={{ cursor: 'pointer' }}>
                Logout
              </a>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  );
}
