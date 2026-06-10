import { createContext, useContext, useState } from 'react';

const STORAGE_KEY = 'PortalAdminHasLoggedIn';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [isLoggedIn, setIsLoggedIn] = useState(
    () => !!localStorage.getItem(STORAGE_KEY)
  );

  const login = () => {
    localStorage.setItem(STORAGE_KEY, 'true');
    setIsLoggedIn(true);
  };

  const logout = () => {
    localStorage.setItem(STORAGE_KEY, '');
    setIsLoggedIn(false);
  };

  return (
    <AuthContext.Provider value={{ isLoggedIn, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext);
  if (context === null) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
