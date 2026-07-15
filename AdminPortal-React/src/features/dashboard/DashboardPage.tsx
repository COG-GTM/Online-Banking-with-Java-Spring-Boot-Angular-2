import { useAuth } from '../../auth/useAuth'

/**
 * Placeholder customer dashboard for the Phase 1 auth shell. It confirms the
 * authenticated session and role-based landing; the full balances + quick-links
 * dashboard (parity with userFront.html) arrives in Phase 2.
 */
export function DashboardPage() {
  const { user } = useAuth()

  return (
    <div>
      <h2 className="mb-3">Welcome, {user?.firstName}</h2>
      <p className="text-muted">You are signed in to the customer portal.</p>

      <div className="row g-3 mt-1">
        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <h5 className="card-title">Primary Account</h5>
              <p className="card-text mb-1">
                Account #: {user?.primaryAccount?.accountNumber ?? '—'}
              </p>
              <p className="card-text">
                Balance: ${user?.primaryAccount?.accountBalance ?? '0.00'}
              </p>
            </div>
          </div>
        </div>
        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <h5 className="card-title">Savings Account</h5>
              <p className="card-text mb-1">
                Account #: {user?.savingsAccount?.accountNumber ?? '—'}
              </p>
              <p className="card-text">
                Balance: ${user?.savingsAccount?.accountBalance ?? '0.00'}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
