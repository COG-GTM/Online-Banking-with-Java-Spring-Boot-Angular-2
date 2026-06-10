import axios from 'axios';

// Empty by default so requests are relative and go through the Vite dev proxy
// (see vite.config.js), avoiding CORS. Override with VITE_API_BASE_URL for
// deployments where the backend is on a different origin.
const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

export function sendCredential(username, password) {
  const params = new URLSearchParams();
  params.append('username', username);
  params.append('password', password);

  return axios.post(`${BASE_URL}/index`, params, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    withCredentials: true,
  });
}

export function logout() {
  return axios.get(`${BASE_URL}/logout`, { withCredentials: true });
}
