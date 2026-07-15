import { createBrowserRouter, Navigate } from 'react-router-dom'
import { RequireAuth } from '../components/RequireAuth'
import { RootRedirect } from '../components/RootRedirect'
import { AppLayout } from '../components/AppLayout'
import { AdminLayout } from '../components/AdminLayout'
import { LoginPage } from '../features/auth/LoginPage'
import { DashboardPage } from '../features/dashboard/DashboardPage'
import { AdminHomePage } from '../features/admin/AdminHomePage'

export const router = createBrowserRouter([
  { path: '/', element: <RootRedirect /> },
  { path: '/login', element: <LoginPage /> },
  {
    element: <RequireAuth role="ROLE_USER" />,
    children: [
      {
        path: '/app',
        element: <AppLayout />,
        children: [{ index: true, element: <DashboardPage /> }],
      },
    ],
  },
  {
    element: <RequireAuth role="ROLE_ADMIN" />,
    children: [
      {
        path: '/admin',
        element: <AdminLayout />,
        children: [{ index: true, element: <AdminHomePage /> }],
      },
    ],
  },
  { path: '*', element: <Navigate to="/" replace /> },
])
