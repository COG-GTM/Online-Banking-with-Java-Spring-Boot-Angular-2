import { useCallback, useState, type ReactNode } from 'react';
import { AuthContext, STORAGE_KEY } from './authContext';

// The raw `PortalAdminHasLoggedIn` value is the single source of truth, mirroring
// the Angular app which recomputes from localStorage on every reload. Three states
// matter: null (never logged in), '' (logged out) and 'true' (logged in).
export function AuthProvider({ children }: { children: ReactNode }) {
  const [authValue, setAuthValue] = useState<string | null>(() =>
    localStorage.getItem(STORAGE_KEY),
  );

  const setLoggedIn = useCallback((value: boolean) => {
    const stored = value ? 'true' : '';
    localStorage.setItem(STORAGE_KEY, stored);
    setAuthValue(stored);
  }, []);

  return (
    <AuthContext.Provider value={{ authValue, setLoggedIn }}>
      {children}
    </AuthContext.Provider>
  );
}
