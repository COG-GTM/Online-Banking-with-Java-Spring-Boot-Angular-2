import axios from 'axios';

const BASE_URL = 'http://localhost:8080';

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
