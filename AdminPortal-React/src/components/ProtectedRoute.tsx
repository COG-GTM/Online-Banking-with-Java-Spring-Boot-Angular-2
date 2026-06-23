import { Navigate } from 'react-router-dom';
import type { ReactNode } from 'react';
import { useAuthContext } from '../context/authContext';

export function ProtectedRoute({ children }: { children: ReactNode }) {
  const { authValue } = useAuthContext();

  // Only redirect after an explicit logout (''); a fresh visit (null) or an
  // active session ('true') is allowed, matching the Angular app's behavior.
  if (authValue === '') {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}
