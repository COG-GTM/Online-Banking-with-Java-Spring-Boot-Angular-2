import { useCallback, useState, type ReactNode } from 'react';
import { AuthContext, AUTH_STORAGE_KEY } from './authContext';
import * as authService from '../services/authService';

function readInitialLoggedIn(): boolean {
  const value = localStorage.getItem(AUTH_STORAGE_KEY);
  return value !== '' && value !== null;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [loggedIn, setLoggedIn] = useState<boolean>(readInitialLoggedIn);

  const login = useCallback(
    async (username: string, password: string, signal?: AbortSignal) => {
      await authService.sendCredential(username, password, signal);
      localStorage.setItem(AUTH_STORAGE_KEY, 'true');
      setLoggedIn(true);
    },
    [],
  );

  const logout = useCallback(async (signal?: AbortSignal) => {
    try {
      await authService.logout(signal);
    } finally {
      localStorage.setItem(AUTH_STORAGE_KEY, '');
      setLoggedIn(false);
    }
  }, []);

  return (
    <AuthContext.Provider value={{ loggedIn, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
