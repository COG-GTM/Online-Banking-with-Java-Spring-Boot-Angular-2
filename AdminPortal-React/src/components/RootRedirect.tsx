import { Navigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { homePathForUser } from '../auth/roles'
import { Loading } from './Loading'

/** Sends the visitor to their role-appropriate home, or to /login. */
export function RootRedirect() {
  const { user, status } = useAuth()

  if (status === 'loading') {
    return <Loading />
  }

  return <Navigate to={user ? homePathForUser(user) : '/login'} replace />
}
