import { Outlet } from 'react-router-dom'
import { AppNavbar } from './AppNavbar'

export function AdminLayout() {
  return (
    <>
      <AppNavbar variant="admin" brand="Admin Portal" />
      <main className="container py-4">
        <Outlet />
      </main>
    </>
  )
}
