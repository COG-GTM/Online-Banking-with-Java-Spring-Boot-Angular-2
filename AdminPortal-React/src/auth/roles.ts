import type { AppUser, Role } from '../types'

export function hasRole(user: AppUser | null, role: Role): boolean {
  return !!user && user.roles.includes(role)
}

/** Landing route for a user based on their primary role. */
export function homePathForUser(user: AppUser | null): string {
  if (hasRole(user, 'ROLE_ADMIN')) {
    return '/admin'
  }
  if (hasRole(user, 'ROLE_USER')) {
    return '/app'
  }
  return '/login'
}
