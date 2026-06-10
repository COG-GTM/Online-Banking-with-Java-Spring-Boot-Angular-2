import client from './client';
import type { AccountDetails, AccountDto, DepositRequest, WithdrawRequest } from '../types';

export async function getPrimaryAccount(): Promise<AccountDetails> {
  const res = await client.get<AccountDetails>('/api/account/primary');
  return res.data;
}

export async function getSavingsAccount(): Promise<AccountDetails> {
  const res = await client.get<AccountDetails>('/api/account/savings');
  return res.data;
}

export async function deposit(data: DepositRequest): Promise<AccountDto> {
  const res = await client.post<AccountDto>('/api/account/deposit', data);
  return res.data;
}

export async function withdraw(data: WithdrawRequest): Promise<AccountDto> {
  const res = await client.post<AccountDto>('/api/account/withdraw', data);
  return res.data;
}
