import client from './client';
import type { AppointmentDto, TransactionDto, UserDto } from '../types';

export async function getUsers(): Promise<UserDto[]> {
  const res = await client.get<UserDto[]>('/api/user/all');
  return res.data;
}

export async function getPrimaryTransactions(username: string): Promise<TransactionDto[]> {
  const res = await client.get<TransactionDto[]>('/api/user/primary/transaction', {
    params: { username },
  });
  return res.data;
}

export async function getSavingsTransactions(username: string): Promise<TransactionDto[]> {
  const res = await client.get<TransactionDto[]>('/api/user/savings/transaction', {
    params: { username },
  });
  return res.data;
}

export async function enableUser(username: string): Promise<void> {
  await client.get(`/api/user/${encodeURIComponent(username)}/enable`);
}

export async function disableUser(username: string): Promise<void> {
  await client.get(`/api/user/${encodeURIComponent(username)}/disable`);
}

export async function getAllAppointments(): Promise<AppointmentDto[]> {
  const res = await client.get<AppointmentDto[]>('/api/appointment/all');
  return res.data;
}

export async function confirmAppointment(id: number): Promise<void> {
  await client.get(`/api/appointment/${id}/confirm`);
}
