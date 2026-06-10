import { api } from './api';
import type { Transaction, User } from '../types';

// Mirrors AdminPortal/src/app/user.service.ts
export function useUsers() {
  const getUsers = () => api.get<User[]>('/api/user/all');

  const getPrimaryTransactions = (username: string) =>
    api.get<Transaction[]>('/api/user/primary/transaction', {
      params: { username },
    });

  const getSavingsTransactions = (username: string) =>
    api.get<Transaction[]>('/api/user/savings/transaction', {
      params: { username },
    });

  const enableUser = (username: string) =>
    api.get(`/api/user/${username}/enable`);

  const disableUser = (username: string) =>
    api.get(`/api/user/${username}/disable`);

  return {
    getUsers,
    getPrimaryTransactions,
    getSavingsTransactions,
    enableUser,
    disableUser,
  };
}
