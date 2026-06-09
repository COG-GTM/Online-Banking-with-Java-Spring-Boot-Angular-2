import type { ReactNode } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import Navbar from '../components/common/Navbar';
import ProtectedRoute from '../components/common/ProtectedRoute';

import LoginPage from '../components/auth/LoginPage';
import SignupPage from '../components/auth/SignupPage';

import DashboardPage from '../components/dashboard/DashboardPage';
import PrimaryAccountPage from '../components/accounts/PrimaryAccountPage';
import SavingsAccountPage from '../components/accounts/SavingsAccountPage';
import DepositPage from '../components/transactions/DepositPage';
import WithdrawPage from '../components/transactions/WithdrawPage';
import BetweenAccountsPage from '../components/transfers/BetweenAccountsPage';
import ExternalTransferPage from '../components/transfers/ExternalTransferPage';
import RecipientListPage from '../components/transfers/RecipientListPage';
import AppointmentPage from '../components/appointments/AppointmentPage';
import ProfilePage from '../components/profile/ProfilePage';

import AdminLoginPage from '../components/admin/AdminLoginPage';
import UserAccountList from '../components/admin/UserAccountList';
import PrimaryTransactionView from '../components/admin/PrimaryTransactionView';
import SavingsTransactionView from '../components/admin/SavingsTransactionView';
import AppointmentManagement from '../components/admin/AppointmentManagement';

function Shell({ children, admin = false }: { children: ReactNode; admin?: boolean }) {
  return (
    <ProtectedRoute requireAdmin={admin}>
      <Navbar />
      <main className="page-content">{children}</main>
    </ProtectedRoute>
  );
}

export default function AppRoutes() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route path="/admin/login" element={<AdminLoginPage />} />

      {/* User (protected) */}
      <Route path="/dashboard" element={<Shell><DashboardPage /></Shell>} />
      <Route path="/accounts/primary" element={<Shell><PrimaryAccountPage /></Shell>} />
      <Route path="/accounts/savings" element={<Shell><SavingsAccountPage /></Shell>} />
      <Route path="/accounts/deposit" element={<Shell><DepositPage /></Shell>} />
      <Route path="/accounts/withdraw" element={<Shell><WithdrawPage /></Shell>} />
      <Route path="/transfers/between" element={<Shell><BetweenAccountsPage /></Shell>} />
      <Route path="/transfers/external" element={<Shell><ExternalTransferPage /></Shell>} />
      <Route path="/transfers/recipients" element={<Shell><RecipientListPage /></Shell>} />
      <Route path="/appointments/new" element={<Shell><AppointmentPage /></Shell>} />
      <Route path="/profile" element={<Shell><ProfilePage /></Shell>} />

      {/* Admin (protected + ADMIN) */}
      <Route path="/admin/users" element={<Shell admin><UserAccountList /></Shell>} />
      <Route path="/admin/users/:username/primary" element={<Shell admin><PrimaryTransactionView /></Shell>} />
      <Route path="/admin/users/:username/savings" element={<Shell admin><SavingsTransactionView /></Shell>} />
      <Route path="/admin/appointments" element={<Shell admin><AppointmentManagement /></Shell>} />

      {/* Fallback */}
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
