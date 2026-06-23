export interface Account {
  accountBalance: number;
}

export interface User {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  primaryAccount: Account;
  savingsAccount: Account;
  enabled: boolean;
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
  user: User;
  date: string;
  description: string;
  confirmed: boolean;
}
