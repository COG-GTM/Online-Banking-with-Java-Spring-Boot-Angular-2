import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function Navbar() {
  const { isAuthenticated, role, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <Link to="/dashboard" className="navbar-brand">
        Online Banking
      </Link>

      {isAuthenticated && role === 'USER' && (
        <div className="navbar-links">
          <Link to="/dashboard">Dashboard</Link>
          <Link to="/accounts/primary">Primary</Link>
          <Link to="/accounts/savings">Savings</Link>
          <Link to="/accounts/deposit">Deposit</Link>
          <Link to="/accounts/withdraw">Withdraw</Link>
          <Link to="/transfers/between">Transfer</Link>
          <Link to="/transfers/external">Send</Link>
          <Link to="/transfers/recipients">Recipients</Link>
          <Link to="/appointments/new">Appointment</Link>
          <Link to="/profile">Profile</Link>
        </div>
      )}

      {isAuthenticated && role === 'ADMIN' && (
        <div className="navbar-links">
          <Link to="/admin/users">Users</Link>
          <Link to="/admin/appointments">Appointments</Link>
        </div>
      )}

      <div className="navbar-right">
        {isAuthenticated ? (
          <>
            <span className="navbar-user">{user?.username}</span>
            <button type="button" onClick={handleLogout}>
              Logout
            </button>
          </>
        ) : (
          <Link to="/login">Login</Link>
        )}
      </div>
    </nav>
  );
}
