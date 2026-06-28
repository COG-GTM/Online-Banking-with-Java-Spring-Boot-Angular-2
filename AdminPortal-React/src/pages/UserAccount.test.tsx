import { afterEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import UserAccount from './UserAccount';
import * as userService from '../services/userService';
import type { User } from '../types/user';

const users: User[] = [
  {
    username: 'jdoe',
    firstName: 'John',
    lastName: 'Doe',
    email: 'jdoe@example.com',
    phone: '555-1000',
    enabled: true,
    primaryAccount: { accountBalance: 1200 },
    savingsAccount: { accountBalance: 8000 },
  },
  {
    username: 'asmith',
    firstName: 'Alice',
    lastName: 'Smith',
    email: 'asmith@example.com',
    phone: '555-2000',
    enabled: false,
    primaryAccount: { accountBalance: 50 },
    savingsAccount: { accountBalance: 300 },
  },
];

function renderPage() {
  return render(
    <MemoryRouter>
      <UserAccount />
    </MemoryRouter>,
  );
}

describe('UserAccount', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('shows a loading state then renders a row per user', async () => {
    vi.spyOn(userService, 'getUsers').mockResolvedValue(users);

    renderPage();
    expect(screen.getByText(/loading users/i)).toBeInTheDocument();

    expect(await screen.findByText('jdoe')).toBeInTheDocument();
    expect(screen.getByText('asmith')).toBeInTheDocument();
  });

  it('renders balance cells as links to the transaction routes', async () => {
    vi.spyOn(userService, 'getUsers').mockResolvedValue(users);

    renderPage();
    await screen.findByText('jdoe');

    const primaryLink = screen.getByRole('link', { name: '1200' });
    expect(primaryLink).toHaveAttribute('href', '/primaryTransaction/jdoe');
    const savingsLink = screen.getByRole('link', { name: '8000' });
    expect(savingsLink).toHaveAttribute('href', '/savingsTransaction/jdoe');
  });

  it('shows Disable for enabled users and Enable for disabled users', async () => {
    vi.spyOn(userService, 'getUsers').mockResolvedValue(users);

    renderPage();
    await screen.findByText('jdoe');

    const enabledRow = screen.getByText('jdoe').closest('tr')!;
    expect(within(enabledRow).getByText('Disable')).toBeInTheDocument();

    const disabledRow = screen.getByText('asmith').closest('tr')!;
    expect(within(disabledRow).getByText('Enable')).toBeInTheDocument();
  });

  it('disables a user and refreshes the list', async () => {
    const getUsersSpy = vi
      .spyOn(userService, 'getUsers')
      .mockResolvedValue(users);
    const disableSpy = vi
      .spyOn(userService, 'disableUser')
      .mockResolvedValue(undefined);

    renderPage();
    await screen.findByText('jdoe');

    const enabledRow = screen.getByText('jdoe').closest('tr')!;
    await userEvent.click(within(enabledRow).getByText('Disable'));

    expect(disableSpy).toHaveBeenCalledWith('jdoe');
    await waitFor(() => expect(getUsersSpy).toHaveBeenCalledTimes(2));
  });

  it('renders an error message when loading fails', async () => {
    vi.spyOn(userService, 'getUsers').mockRejectedValue(new Error('boom'));

    renderPage();

    expect(await screen.findByText('boom')).toBeInTheDocument();
  });
});
