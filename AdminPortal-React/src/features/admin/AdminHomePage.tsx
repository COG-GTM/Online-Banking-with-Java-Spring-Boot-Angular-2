import { useAuth } from '../../auth/useAuth'

/**
 * Placeholder admin landing for the Phase 1 auth shell. Confirms the admin
 * session and role-based landing; the user-accounts table (parity with the
 * Angular UserAccountComponent) arrives in Phase 10.
 */
export function AdminHomePage() {
  const { user } = useAuth()

  return (
    <div>
      <h2 className="mb-3">Admin Portal</h2>
      <p className="text-muted">
        Signed in as {user?.username} ({user?.roles.join(', ')}).
      </p>
      <div className="alert alert-info">
        Admin management screens are migrated in later phases (users, transactions,
        appointments).
      </div>
    </div>
  )
}
