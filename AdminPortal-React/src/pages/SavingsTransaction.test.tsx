import { afterEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import SavingsTransaction from './SavingsTransaction';

function renderAt(username: string) {
  return render(
    <MemoryRouter initialEntries={[`/savingsTransaction/${username}`]}>
      <Routes>
        <Route
          path="/savingsTransaction/:username"
          element={<SavingsTransaction />}
        />
      </Routes>
    </MemoryRouter>,
  );
}

describe('SavingsTransaction page', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('renders fetched transactions in a table', async () => {
    const data = [
      {
        id: 1,
        date: '2023-01-09T00:00:00',
        description: 'ATM Deposit',
        type: 'Credit',
        status: 'Complete',
        amount: 250,
        availableBalance: 1250,
      },
    ];
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify(data), { status: 200 }),
    );

    renderAt('alice');

    expect(
      screen.getByRole('heading', { name: /Savings Account Transaction List/i }),
    ).toBeInTheDocument();

    await waitFor(() =>
      expect(screen.getByText('ATM Deposit')).toBeInTheDocument(),
    );
    expect(screen.getByText('01/09/2023')).toBeInTheDocument();
    expect(screen.getByText('1250')).toBeInTheDocument();
  });

  it('shows the empty state when there are no transactions', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify([]), { status: 200 }),
    );

    renderAt('bob');

    await waitFor(() =>
      expect(
        screen.getByText(/No savings transactions found/i),
      ).toBeInTheDocument(),
    );
  });

  it('shows an error message when the request fails', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response('boom', { status: 500, statusText: 'Server Error' }),
    );

    renderAt('carol');

    await waitFor(() =>
      expect(screen.getByRole('alert')).toHaveTextContent(
        /Failed to load savings transactions/i,
      ),
    );
  });
});
