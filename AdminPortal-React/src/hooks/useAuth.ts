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

    // `/index` is Spring Security's form-login endpoint: it returns a 302 to
    // `/userFront` on success and `/index?error` on failure. The browser follows
    // the redirect and axios resolves with 200 in both cases, so inspect the
    // final URL to decide whether authentication actually succeeded.
    const finalUrl: string =
      (res.request as XMLHttpRequest | undefined)?.responseURL ?? '';
    if (finalUrl && !finalUrl.includes('/userFront')) {
      throw new Error('Invalid username or password');
    }

    return res;
  };

  const logout = () => api.get('/logout');

  return { login, logout };
}
