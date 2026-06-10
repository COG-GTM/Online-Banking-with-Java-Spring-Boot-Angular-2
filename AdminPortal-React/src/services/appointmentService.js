import axios from 'axios';

// Empty by default so requests are relative and go through the Vite dev proxy
// (see vite.config.js), avoiding CORS. Override with VITE_API_BASE_URL for
// deployments where the backend is on a different origin.
const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

export function getAppointmentList() {
  return axios.get(`${BASE_URL}/api/appointment/all`, { withCredentials: true });
}

export function confirmAppointment(id) {
  return axios.get(`${BASE_URL}/api/appointment/${id}/confirm`, {
    withCredentials: true,
  });
}
