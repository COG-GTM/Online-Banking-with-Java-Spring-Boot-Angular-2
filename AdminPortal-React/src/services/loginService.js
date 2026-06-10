import axios from 'axios';

const BASE_URL = 'http://localhost:8080';

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
