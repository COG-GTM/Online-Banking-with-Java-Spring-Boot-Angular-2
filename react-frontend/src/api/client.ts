import axios from 'axios';

// Centralised axios instance. baseURL defaults to same-origin so the Vite dev
// proxy (/api -> :8080) is used in development; set VITE_API_URL for builds
// that talk to a backend on a different origin.
const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// On 401 the session is gone/expired: bounce the user to the login screen
// (unless they are already on an auth page).
client.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status;
    const path = window.location.pathname;
    if (status === 401 && !path.startsWith('/login') && !path.startsWith('/signup') && !path.startsWith('/admin/login')) {
      window.location.assign('/login');
    }
    return Promise.reject(error);
  },
);

export default client;
