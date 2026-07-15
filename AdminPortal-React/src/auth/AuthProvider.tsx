import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import type { AppUser, LoginRequest } from '../types'
import * as authApi from './authApi'
import { AuthContext } from './AuthContext'
import type { AuthStatus } from './AuthContext'

/**
 * Holds the authenticated session, reusing the Spring Security session cookie
 * (the same model the legacy Angular admin uses). On mount it resolves the
 * current session via {@code GET /api/auth/me} so guards can render correctly
 * after a page refresh.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AppUser | null>(null)
  const [status, setStatus] = useState<AuthStatus>('loading')

  useEffect(() => {
    let active = true
    authApi
      .fetchMe()
      .then((me) => {
        if (!active) return
        setUser(me)
        setStatus(me ? 'authenticated' : 'unauthenticated')
      })
      .catch(() => {
        if (!active) return
        setUser(null)
        setStatus('unauthenticated')
      })
    return () => {
      active = false
    }
  }, [])

  const login = useCallback(async (credentials: LoginRequest) => {
    const me = await authApi.login(credentials)
    setUser(me)
    setStatus('authenticated')
    return me
  }, [])

  const logout = useCallback(async () => {
    await authApi.logout()
    setUser(null)
    setStatus('unauthenticated')
  }, [])

  const value = useMemo(
    () => ({ user, status, login, logout }),
    [user, status, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
