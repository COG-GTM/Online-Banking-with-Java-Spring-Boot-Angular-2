import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import * as authApi from '../api/authApi';
import type { LoginRequest, Role, UserDto } from '../types';

interface AuthContextValue {
  user: UserDto | null;
  role: Role;
  isAuthenticated: boolean;
  loading: boolean;
  login: (data: LoginRequest) => Promise<UserDto>;
  logout: () => Promise<void>;
  refresh: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function deriveRole(user: UserDto | null): Role {
  if (!user) return null;
  if (user.roles?.includes('ROLE_ADMIN')) return 'ADMIN';
  return 'USER';
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserDto | null>(null);
  const [loading, setLoading] = useState(true);

  const refresh = async () => {
    try {
      const current = await authApi.getCurrentUser();
      setUser(current);
    } catch {
      setUser(null);
    }
  };

  useEffect(() => {
    (async () => {
      await refresh();
      setLoading(false);
    })();
  }, []);

  const login = async (data: LoginRequest) => {
    const loggedIn = await authApi.login(data);
    setUser(loggedIn);
    return loggedIn;
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } finally {
      setUser(null);
    }
  };

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      role: deriveRole(user),
      isAuthenticated: !!user,
      loading,
      login,
      logout,
      refresh,
    }),
    [user, loading],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
}
