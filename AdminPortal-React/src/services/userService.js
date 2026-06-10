import axios from 'axios';

// Empty by default so requests are relative and go through the Vite dev proxy
// (see vite.config.js), avoiding CORS. Override with VITE_API_BASE_URL for
// deployments where the backend is on a different origin.
const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

export function getUsers() {
  return axios.get(`${BASE_URL}/api/user/all`, { withCredentials: true });
}

export function getPrimaryTransactionList(username) {
  return axios.get(`${BASE_URL}/api/user/primary/transaction`, {
    params: { username },
    withCredentials: true,
  });
}

export function getSavingsTransactionList(username) {
  return axios.get(`${BASE_URL}/api/user/savings/transaction`, {
    params: { username },
    withCredentials: true,
  });
}

export function enableUser(username) {
  return axios.get(`${BASE_URL}/api/user/${username}/enable`, {
    withCredentials: true,
  });
}

export function disableUser(username) {
  return axios.get(`${BASE_URL}/api/user/${username}/disable`, {
    withCredentials: true,
  });
}
