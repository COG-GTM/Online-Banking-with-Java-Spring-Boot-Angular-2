import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { Navbar } from './Navbar';
import { AuthProvider } from '../context/AuthProvider';
import { AUTH_STORAGE_KEY } from '../context/authContext';

function renderNavbar() {
  return render(
    <MemoryRouter>
      <AuthProvider>
        <Navbar />
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('Navbar', () => {
  beforeEach(() => localStorage.clear());
  afterEach(() => localStorage.clear());

  it('always renders the brand linking to /login', () => {
    renderNavbar();
    const brand = screen.getByRole('link', { name: /admin portal/i });
    expect(brand).toBeInTheDocument();
    expect(brand).toHaveAttribute('href', '/login');
  });

  it('hides nav items and logout when logged out', () => {
    renderNavbar();
    expect(screen.getByText(/user account/i).closest('li')).toHaveStyle({
      display: 'none',
    });
    expect(screen.getByText(/logout/i).closest('li')).toHaveStyle({
      display: 'none',
    });
  });

  it('shows nav items and logout when logged in', () => {
    localStorage.setItem(AUTH_STORAGE_KEY, 'true');
    renderNavbar();
    expect(screen.getByText(/user account/i).closest('li')).not.toHaveStyle({
      display: 'none',
    });
    expect(screen.getByText(/logout/i).closest('li')).not.toHaveStyle({
      display: 'none',
    });
  });
});
