import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { ProtectedRoute } from './components/ProtectedRoute';
import { LoginPage } from './pages/LoginPage';
import { UserAccountPage } from './pages/UserAccountPage';
import { PrimaryTransactionPage } from './pages/PrimaryTransactionPage';
import { SavingsTransactionPage } from './pages/SavingsTransactionPage';
import { AppointmentPage } from './pages/AppointmentPage';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="container">
          <Navbar />
          <Routes>
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="/login" element={<LoginPage />} />
            <Route
              path="/userAccount"
              element={
                <ProtectedRoute>
                  <UserAccountPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/primaryTransaction/:username"
              element={
                <ProtectedRoute>
                  <PrimaryTransactionPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/savingsTransaction/:username"
              element={
                <ProtectedRoute>
                  <SavingsTransactionPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/appointment"
              element={
                <ProtectedRoute>
                  <AppointmentPage />
                </ProtectedRoute>
              }
            />
          </Routes>
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
