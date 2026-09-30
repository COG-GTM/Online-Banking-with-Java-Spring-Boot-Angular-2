export interface Account {
  id: number;
  accountNumber: number;
  accountBalance: number;
}

export interface User {
  userId: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  enabled: boolean;
  primaryAccount: Account;
  savingsAccount: Account;
}

export interface Transaction {
  id: number;
  date: string;
  description: string;
  type: string;
  status: string;
  amount: number;
  availableBalance: number;
}

export interface Appointment {
  id: number;
  date: string;
  location: string;
  description: string;
  confirmed: boolean;
  user: Pick<User, 'username'>;
}
