import axios from 'axios';

const BASE_URL = 'http://localhost:8080';

export function getAppointmentList() {
  return axios.get(`${BASE_URL}/api/appointment/all`, { withCredentials: true });
}

export function confirmAppointment(id) {
  return axios.get(`${BASE_URL}/api/appointment/${id}/confirm`, {
    withCredentials: true,
  });
}
