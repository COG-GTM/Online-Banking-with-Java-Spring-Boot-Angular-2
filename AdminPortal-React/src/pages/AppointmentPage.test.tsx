import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import AppointmentPage from './AppointmentPage';
import type { Appointment } from '../types/appointment';

const appointments: Appointment[] = [
  {
    id: 1,
    date: '2024-03-15T14:30:00',
    location: 'HQ',
    description: 'Loan consultation',
    confirmed: false,
    user: { username: 'jdoe' },
  },
  {
    id: 2,
    date: '2024-03-16T09:00:00',
    location: 'Branch',
    description: 'Account review',
    confirmed: true,
    user: { username: 'asmith' },
  },
];

function renderPage() {
  return render(
    <MemoryRouter>
      <AppointmentPage />
    </MemoryRouter>,
  );
}

describe('AppointmentPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('renders the appointment rows after loading', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify(appointments), { status: 200 }),
    );

    renderPage();

    expect(screen.getByText('Loading appointments...')).toBeInTheDocument();

    expect(await screen.findByText('Loan consultation')).toBeInTheDocument();
    expect(screen.getByText('jdoe')).toBeInTheDocument();
    expect(screen.getByText('asmith')).toBeInTheDocument();
  });

  it('shows an empty state when there are no appointments', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify([]), { status: 200 }),
    );

    renderPage();

    expect(await screen.findByText('No appointments found.')).toBeInTheDocument();
  });

  it('shows an error state when the request fails', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(null, { status: 500 }),
    );

    renderPage();

    expect(await screen.findByText(/Failed to load/)).toBeInTheDocument();
  });

  it('confirms an appointment and refreshes the list', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockImplementation((input) => {
      const url = String(input);
      if (url.includes('/confirm')) {
        return Promise.resolve(new Response(null, { status: 200 }));
      }
      return Promise.resolve(
        new Response(JSON.stringify(appointments), { status: 200 }),
      );
    });

    renderPage();

    const row = (await screen.findByText('Loan consultation')).closest('tr')!;
    await userEvent.click(within(row).getByText('Confirm'));

    await waitFor(() => {
      expect(
        fetchMock.mock.calls.some((call) =>
          String(call[0]).includes('/api/appointment/1/confirm'),
        ),
      ).toBe(true);
    });

    // initial load + refresh load + confirm = at least 3 calls
    expect(fetchMock.mock.calls.length).toBeGreaterThanOrEqual(3);
  });
});
