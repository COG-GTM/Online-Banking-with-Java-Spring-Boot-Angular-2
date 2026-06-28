import { Navigate, Route, Routes } from 'react-router-dom';
import Navbar from './components/Navbar';
import SavingsTransaction from './pages/SavingsTransaction';

// Shared app shell ported from AdminPortal/src/app/app.component.html
// (<app-navbar> + <router-outlet> inside .container).
// NOTE: parallel migration — only the /savingsTransaction/:username route is
// wired here. Other routes (login, userAccount, primaryTransaction, appointment)
// are owned by other migration slices.
export default function App() {
  return (
    <div className="container">
      <Navbar />
      <Routes>
        <Route
          path="/savingsTransaction/:username"
          element={<SavingsTransaction />}
        />
        <Route
          path="*"
          element={<Navigate to="/savingsTransaction/demo" replace />}
        />
      </Routes>
    </div>
  );
}
