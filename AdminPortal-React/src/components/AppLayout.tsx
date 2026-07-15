import { Outlet } from 'react-router-dom'
import { AppNavbar } from './AppNavbar'

export function AppLayout() {
  return (
    <>
      <AppNavbar variant="customer" brand="Online Banking" />
      <main className="container py-4">
        <Outlet />
      </main>
    </>
  )
}
