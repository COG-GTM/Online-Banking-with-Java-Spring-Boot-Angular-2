import { ApiError, apiClient } from '../lib/apiClient'
import type { AppUser, LoginRequest } from '../types'

export function login(body: LoginRequest): Promise<AppUser> {
  return apiClient.post<AppUser>('/api/auth/login', body)
}

export async function logout(): Promise<void> {
  await apiClient.post<void>('/api/auth/logout')
}

/** Returns the current user, or null when there is no authenticated session. */
export async function fetchMe(): Promise<AppUser | null> {
  try {
    return await apiClient.get<AppUser>('/api/auth/me')
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) {
      return null
    }
    throw error
  }
}
