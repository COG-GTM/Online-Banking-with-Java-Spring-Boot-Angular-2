import client from './client';
import type { AppointmentDto, AppointmentRequest } from '../types';

export async function getAppointments(): Promise<AppointmentDto[]> {
  const res = await client.get<AppointmentDto[]>('/api/appointments');
  return res.data;
}

export async function createAppointment(data: AppointmentRequest): Promise<AppointmentDto> {
  const res = await client.post<AppointmentDto>('/api/appointments', data);
  return res.data;
}
