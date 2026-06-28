export interface Account {
  accountBalance: number;
}

export interface User {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  enabled: boolean;
  primaryAccount: Account;
  savingsAccount: Account;
}
