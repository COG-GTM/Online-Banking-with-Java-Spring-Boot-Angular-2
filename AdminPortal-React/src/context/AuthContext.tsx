import { useCallback, useState, type ReactNode } from 'react';
import { AuthContext, STORAGE_KEY } from './authContext';

function readInitialState(): boolean {
  const value = localStorage.getItem(STORAGE_KEY);
  return value !== null && value !== '';
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
