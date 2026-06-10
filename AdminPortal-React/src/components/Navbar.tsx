import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useAuthContext } from '../context/authContext';

export function Navbar() {
  const { logout } = useAuth();
  const { isLoggedIn, setLoggedIn } = useAuthContext();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await logout();
    } catch (err) {
      console.log(err);
    } finally {
      setLoggedIn(false);
      navigate('/login');
    }
  };

  return (
    <nav className="navbar navbar-expand navbar-clean navbar-default">
      <div className="container-fluid">
        <div className="navbar-header">
          <NavLink className="navbar-brand" to="/login">
            Admin Portal
          </NavLink>
        </div>

        <div className="collapse navbar-collapse">
          {isLoggedIn && (
            <>
              <ul className="nav navbar-nav">
                <li className="nav-item">
                  <NavLink className="nav-link" to="/userAccount">
                    User Account
                  </NavLink>
                </li>
                <li className="nav-item">
                  <NavLink className="nav-link" to="/appointment">
                    Appointment
                  </NavLink>
                </li>
              </ul>
              <ul className="nav navbar-nav navbar-right ms-auto">
                <li className="nav-item">
                  <a
                    className="nav-link"
                    onClick={handleLogout}
                    style={{ cursor: 'pointer' }}
                  >
                    Logout
                  </a>
                </li>
              </ul>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
