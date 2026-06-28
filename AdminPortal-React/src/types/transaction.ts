export interface Transaction {
  id?: number;
  date: string;
  description: string;
  type: string;
  status: string;
  amount: number;
  availableBalance: number;
}
