import { Link } from "react-router-dom";
import { API_BASE_URL } from "../services/api";

/**
 * Port of the Angular `NavbarComponent` (AdminPortal/src/app/navbar/navbar.component.*).
 *
 * Brand "Admin Portal" links to /login; nav links to /userAccount and
 * /appointment plus a Logout action are hidden until the admin has logged in
 * (tracked via the `PortalAdminHasLoggedIn` localStorage flag, as in Angular).
 *
 * NOTE: /userAccount, /appointment and /login are owned by other migration
 * slices; this navbar only renders the shared shell so the Primary Transaction
 * page builds and renders standalone.
 */
export default function Navbar() {
  const loggedIn = !!localStorage.getItem("PortalAdminHasLoggedIn");
  const display = loggedIn ? undefined : "none";

  async function logout() {
    try {
      await fetch(`${API_BASE_URL}/logout`, {
        method: "GET",
        credentials: "include",
      });
    } catch {
      // ignore network errors on logout
    }
    localStorage.setItem("PortalAdminHasLoggedIn", "");
    window.location.assign("/login");
  }

  return (
    <nav className="navbar navbar-clean">
      <div className="container-fluid">
        <div className="navbar-header">
          <Link className="navbar-brand" to="/login">
            Admin Portal
          </Link>
        </div>

        <div className="collapse navbar-collapse">
          <ul className="nav navbar-nav">
            <li style={{ display }}>
              <Link to="/userAccount"> User Account</Link>
            </li>
            <li style={{ display }}>
              <Link to="/appointment"> Appointment</Link>
            </li>
          </ul>
          <ul className="nav navbar-nav navbar-right">
            <li style={{ display }}>
              <a onClick={logout} style={{ cursor: "pointer" }}>
                Logout
              </a>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  );
}
