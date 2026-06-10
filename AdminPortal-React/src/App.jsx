import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import LoginPage from './pages/LoginPage';
import UserAccountPage from './pages/UserAccountPage';
import PrimaryTransactionPage from './pages/PrimaryTransactionPage';
import SavingsTransactionPage from './pages/SavingsTransactionPage';
import AppointmentPage from './pages/AppointmentPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="container">
          <Navbar />
          <Routes>
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/userAccount" element={<UserAccountPage />} />
            <Route
              path="/primaryTransaction/:username"
              element={<PrimaryTransactionPage />}
            />
            <Route
              path="/savingsTransaction/:username"
              element={<SavingsTransactionPage />}
            />
            <Route path="/appointment" element={<AppointmentPage />} />
          </Routes>
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}
