import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { hasRole, homePathForUser } from '../auth/roles'
import type { Role } from '../types'
import { Loading } from './Loading'

/**
 * Route guard. Redirects unauthenticated users to /login, and users who lack
 * the required role to their own home (parity with the legacy per-audience
 * portals: customers cannot see admin pages and vice versa).
 */
export function RequireAuth({ role }: { role?: Role }) {
  const { user, status } = useAuth()
  const location = useLocation()

  if (status === 'loading') {
    return <Loading />
  }

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (role && !hasRole(user, role)) {
    return <Navigate to={homePathForUser(user)} replace />
  }

  return <Outlet />
}
