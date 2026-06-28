import { Navigate, Route, Routes } from 'react-router-dom';
import Navbar from './components/Navbar';
import UserAccount from './pages/UserAccount';

export default function App() {
  return (
    <div className="container">
      <Navbar />
      <Routes>
        <Route path="/" element={<Navigate to="/userAccount" replace />} />
        <Route path="/userAccount" element={<UserAccount />} />
      </Routes>
    </div>
  );
}
