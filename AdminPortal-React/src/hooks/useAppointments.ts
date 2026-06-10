import { api } from './api';
import type { Appointment } from '../types';

// Mirrors AdminPortal/src/app/appointment.service.ts
export function useAppointments() {
  const getAll = () => api.get<Appointment[]>('/api/appointment/all');

  const confirm = (id: number) => api.get(`/api/appointment/${id}/confirm`);

  return { getAll, confirm };
}
