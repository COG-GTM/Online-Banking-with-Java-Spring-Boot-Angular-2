import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

/**
 * Shared top navbar. Mirrors the legacy header: a brand link plus the signed-in
 * user's name and a logout action. The variant switches accent colour so the
 * customer and admin shells stay visually distinct like the two legacy apps.
 */
export function AppNavbar({
  variant,
  brand,
}: {
  variant: 'customer' | 'admin'
  brand: string
}) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    await logout()
    navigate('/login', { replace: true })
  }

  const navClass = variant === 'admin' ? 'navbar-dark bg-dark' : 'navbar-dark bg-primary'

  return (
    <nav className={`navbar navbar-expand ${navClass}`}>
      <div className="container">
        <span className="navbar-brand mb-0 h1">{brand}</span>
        <div className="d-flex align-items-center gap-3 ms-auto">
          {user && (
            <span className="navbar-text text-white">
              {user.firstName} {user.lastName}{' '}
              <span className="badge bg-light text-dark">{user.username}</span>
            </span>
          )}
          <button className="btn btn-outline-light btn-sm" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </div>
    </nav>
  )
}
