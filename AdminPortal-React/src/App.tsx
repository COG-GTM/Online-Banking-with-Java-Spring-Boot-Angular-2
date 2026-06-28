import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Layout from "./components/Layout";
import PrimaryTransaction from "./pages/PrimaryTransaction";

/**
 * Router for the React Admin Portal.
 *
 * This migration slice owns ONLY `/primaryTransaction/:username`. Other routes
 * (/login, /userAccount, /savingsTransaction/:username, /appointment) are
 * migrated by sibling sessions; they are intentionally not wired up here.
 */
export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route
            path="/primaryTransaction/:username"
            element={<PrimaryTransaction />}
          />
          <Route path="/" element={<Navigate to="/login" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
