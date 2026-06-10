import { api } from './api';

// Mirrors AdminPortal/src/app/login.service.ts
export function useAuth() {
  const login = (username: string, password: string) => {
    const params = new URLSearchParams();
    params.append('username', username);
    params.append('password', password);

    return api.post('/index', params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    });
  };

  const logout = () => api.get('/logout');

  return { login, logout };
}
