import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import AppointmentPage from './pages/AppointmentPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Navigate to="/appointment" replace />} />
          <Route path="/appointment" element={<AppointmentPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
