import { createContext } from 'react'
import type { AppUser, LoginRequest } from '../types'

export type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated'

export interface AuthContextValue {
  user: AppUser | null
  status: AuthStatus
  login: (credentials: LoginRequest) => Promise<AppUser>
  logout: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)
