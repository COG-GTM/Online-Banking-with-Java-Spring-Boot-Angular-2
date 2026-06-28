/**
 * Mirrors the backend `PrimaryTransaction` domain entity
 * (UserFront/.../domain/PrimaryTransaction.java) as serialized to the
 * Admin Portal over `/api/user/primary/transaction`.
 */
export interface Transaction {
  id: number;
  date: string;
  description: string;
  type: string;
  status: string;
  amount: number;
  availableBalance: number;
}
