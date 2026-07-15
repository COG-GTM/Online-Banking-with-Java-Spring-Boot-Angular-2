// Shared DTO types mirroring the UserFront JSON API (see REACT_MIGRATION_STRATEGY.md §3.4).

export type Role = 'ROLE_USER' | 'ROLE_ADMIN'

export interface Account {
  id: number
  accountNumber: number
  accountBalance: number
}

export interface AppUser {
  userId: number
  username: string
  firstName: string
  lastName: string
  email: string
  phone: string
  enabled: boolean
  roles: Role[]
  primaryAccount: Account | null
  savingsAccount: Account | null
}

export interface Transaction {
  id: number
  /** epoch milliseconds, or null when unset */
  date: number | null
  description: string
  type: string
  status: string
  amount: number
  availableBalance: number
}

export interface AccountStatement {
  account: Account | null
  transactions: Transaction[]
}

export interface LoginRequest {
  username: string
  password: string
}
