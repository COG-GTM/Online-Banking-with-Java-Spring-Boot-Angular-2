import { useCallback, useState, type ReactNode } from 'react';
import { AuthContext, STORAGE_KEY } from './authContext';

// Mirrors AdminPortal/src/app/navbar/navbar.component.ts: the navbar treats any
// value other than the empty string (including a missing/null key) as logged-in.
function readInitialState(): boolean {
  return localStorage.getItem(STORAGE_KEY) !== '';
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(readInitialState);

  const setLoggedIn = useCallback((value: boolean) => {
    setIsLoggedIn(value);
    localStorage.setItem(STORAGE_KEY, value ? 'true' : '');
  }, []);

  return (
    <AuthContext.Provider value={{ isLoggedIn, setLoggedIn }}>
      {children}
    </AuthContext.Provider>
  );
}
