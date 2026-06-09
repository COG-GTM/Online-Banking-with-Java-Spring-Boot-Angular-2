// TypeScript mirrors of the backend DTOs (see com.userFront.dto).

export interface AccountDto {
  id: number;
  accountNumber: number;
  accountBalance: number;
}

export interface TransactionDto {
  id: number;
  date: string;
  description: string;
  type: string;
  status: string;
  amount: number;
  availableBalance: number;
}

export interface UserDto {
  userId: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  enabled: boolean;
  roles: string[];
  primaryAccount: AccountDto | null;
  savingsAccount: AccountDto | null;
}

export interface RecipientDto {
  id?: number;
  name: string;
  email: string;
  phone: string;
  accountNumber: string;
  description: string;
}

export interface AppointmentDto {
  id: number;
  date: string;
  location: string;
  description: string;
  confirmed: boolean;
  username: string;
}

export interface ProfileDto {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
}

export interface AccountDetails {
  account: AccountDto;
  transactions: TransactionDto[];
}

// Request payloads
export interface LoginRequest {
  username: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone: string;
}

export interface DepositRequest {
  accountType: string;
  amount: number;
}

export interface WithdrawRequest {
  accountType: string;
  amount: number;
}

export interface BetweenTransferRequest {
  transferFrom: string;
  transferTo: string;
  amount: string;
}

export interface ExternalTransferRequest {
  recipientName: string;
  accountType: string;
  amount: string;
}

export interface AppointmentRequest {
  date: string;
  description: string;
  location?: string;
}

export interface ProfileUpdateRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
}

export type Role = 'ADMIN' | 'USER' | null;
