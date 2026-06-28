import { Navigate, Route, Routes } from 'react-router-dom';
import { Navbar } from './components/Navbar';
import { Login } from './pages/Login';
import { Placeholder } from './pages/Placeholder';

export default function App() {
  return (
    <div className="container">
      <Navbar />
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />
        {/* Routes below are owned by other parallel migration sessions. */}
        <Route
          path="/userAccount"
          element={<Placeholder title="User Account" />}
        />
        <Route
          path="/appointment"
          element={<Placeholder title="Appointment" />}
        />
        <Route
          path="/primaryTransaction/:username"
          element={<Placeholder title="Primary Transaction" />}
        />
        <Route
          path="/savingsTransaction/:username"
          element={<Placeholder title="Savings Transaction" />}
        />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </div>
  );
}
