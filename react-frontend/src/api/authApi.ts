import client from './client';
import type { LoginRequest, SignupRequest, UserDto } from '../types';

export async function login(data: LoginRequest): Promise<UserDto> {
  const res = await client.post<UserDto>('/api/auth/login', data);
  return res.data;
}

export async function signup(data: SignupRequest): Promise<UserDto> {
  const res = await client.post<UserDto>('/api/auth/signup', data);
  return res.data;
}

export async function logout(): Promise<void> {
  await client.post('/api/auth/logout');
}

export async function getCurrentUser(): Promise<UserDto> {
  const res = await client.get<UserDto>('/api/user/me');
  return res.data;
}
