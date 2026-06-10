import { api } from './api';

// Mirrors AdminPortal/src/app/login.service.ts
export function useAuth() {
  const login = async (username: string, password: string) => {
    const params = new URLSearchParams();
    params.append('username', username);
    params.append('password', password);

    const res = await api.post('/index', params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    });

    // `/index` is Spring Security's form-login endpoint. On failure it redirects
    // to the configured `failureUrl` (`/index?error`); on success it redirects to
    // `defaultSuccessUrl` (`/userFront`) OR, when a saved request exists, back to
    // that originally-requested URL (`alwaysUse=false`). The browser follows the
    // redirect and axios resolves with 200 in both cases, so detect failure by
    // the failure URL rather than requiring a specific success URL. A missing
    // final URL is treated as failure so we never mark the user logged in blindly.
    const finalUrl: string =
      (res.request as XMLHttpRequest | undefined)?.responseURL ?? '';
    if (!finalUrl || finalUrl.includes('/index?error')) {
      throw new Error('Invalid username or password');
    }

    return res;
  };

  const logout = () => api.get('/logout');

  return { login, logout };
}
