import type { Appointment } from '../types/appointment';

const API_BASE = 'http://localhost:8080';

export async function getAppointmentList(
  signal?: AbortSignal,
): Promise<Appointment[]> {
  const res = await fetch(`${API_BASE}/api/appointment/all`, {
    method: 'GET',
    credentials: 'include',
    signal,
  });
  if (!res.ok) {
    throw new Error(`Failed to load appointments (${res.status})`);
  }
  return (await res.json()) as Appointment[];
}

export async function confirmAppointment(
  id: number,
  signal?: AbortSignal,
): Promise<void> {
  const res = await fetch(`${API_BASE}/api/appointment/${id}/confirm`, {
    method: 'GET',
    credentials: 'include',
    signal,
  });
  if (!res.ok) {
    throw new Error(`Failed to confirm appointment ${id} (${res.status})`);
  }
}
