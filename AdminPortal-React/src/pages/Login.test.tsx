import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import App from '../App';
import { AuthProvider } from '../context/AuthProvider';
import * as authService from '../services/authService';

function renderApp() {
  return render(
    <MemoryRouter initialEntries={['/login']}>
      <AuthProvider>
        <App />
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('Login page', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('renders the login form', () => {
    renderApp();
    expect(
      screen.getByRole('heading', { name: /please login/i }),
    ).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Username')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
  });

  it('navigates to /userAccount on successful login', async () => {
    const user = userEvent.setup();
    const spy = vi
      .spyOn(authService, 'sendCredential')
      .mockResolvedValue(new Response(null, { status: 200 }));

    renderApp();
    await user.type(screen.getByPlaceholderText('Username'), 'admin');
    await user.type(screen.getByPlaceholderText('Password'), 'pw');
    await user.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() =>
      expect(
        screen.getByRole('heading', { name: /user account/i }),
      ).toBeInTheDocument(),
    );
    expect(spy).toHaveBeenCalledWith('admin', 'pw', expect.any(AbortSignal));
  });

  it('shows an error message when login fails', async () => {
    const user = userEvent.setup();
    vi.spyOn(authService, 'sendCredential').mockRejectedValue(
      new Error('Login failed with status 401'),
    );

    renderApp();
    await user.type(screen.getByPlaceholderText('Username'), 'admin');
    await user.type(screen.getByPlaceholderText('Password'), 'bad');
    await user.click(screen.getByRole('button', { name: /login/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent(
      /invalid username or password/i,
    );
  });
});
