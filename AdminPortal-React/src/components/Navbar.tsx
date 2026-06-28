import { Link } from 'react-router-dom';

export default function Navbar() {
  const loggedIn =
    typeof localStorage !== 'undefined' &&
    !!localStorage.getItem('PortalAdminHasLoggedIn');

  const navItemStyle = { display: loggedIn ? '' : 'none' };

  const handleLogout = () => {
    fetch('http://localhost:8080/logout', {
      method: 'GET',
      credentials: 'include',
    })
      .catch((err) => console.log(err))
      .finally(() => {
        localStorage.setItem('PortalAdminHasLoggedIn', '');
        window.location.assign('/login');
      });
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
            <span className="icon-bar" />
            <span className="icon-bar" />
            <span className="icon-bar" />
          </button>
          <Link className="navbar-brand" to="/login">
            Admin Portal
          </Link>
        </div>

        <div
          className="collapse navbar-collapse"
          id="bs-example-navbar-collapse-1"
        >
          <ul className="nav navbar-nav">
            <li style={navItemStyle}>
              <Link to="/userAccount">
                {' '}
                User Account <span className="sr-only">(current)</span>
              </Link>
            </li>
            <li style={navItemStyle}>
              <Link to="/appointment">
                {' '}
                Appointment <span className="sr-only">(current)</span>
              </Link>
            </li>
          </ul>
          <ul className="nav navbar-nav navbar-right">
            <li style={navItemStyle}>
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
