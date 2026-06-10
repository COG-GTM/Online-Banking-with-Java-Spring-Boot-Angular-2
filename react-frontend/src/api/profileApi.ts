import client from './client';
import type { ProfileDto, ProfileUpdateRequest } from '../types';

export async function getProfile(): Promise<ProfileDto> {
  const res = await client.get<ProfileDto>('/api/user/profile');
  return res.data;
}

export async function updateProfile(data: ProfileUpdateRequest): Promise<ProfileDto> {
  const res = await client.put<ProfileDto>('/api/user/profile', data);
  return res.data;
}
