import { afterEach, describe, expect, it, vi } from 'vitest';
import { confirmAppointment, getAppointmentList } from './appointmentService';

describe('appointmentService', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('getAppointmentList fetches /api/appointment/all with credentials', async () => {
    const data = [
      {
        id: 1,
        date: '2024-01-01T10:00:00Z',
        location: 'HQ',
        description: 'Loan review',
        confirmed: false,
        user: { username: 'jdoe' },
      },
    ];
    const fetchMock = vi
      .spyOn(globalThis, 'fetch')
      .mockResolvedValue(new Response(JSON.stringify(data), { status: 200 }));

    const result = await getAppointmentList();

    expect(result).toEqual(data);
    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8080/api/appointment/all',
      expect.objectContaining({ method: 'GET', credentials: 'include' }),
    );
  });

  it('confirmAppointment calls the confirm endpoint with credentials', async () => {
    const fetchMock = vi
      .spyOn(globalThis, 'fetch')
      .mockResolvedValue(new Response(null, { status: 200 }));

    await confirmAppointment(42);

    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8080/api/appointment/42/confirm',
      expect.objectContaining({ method: 'GET', credentials: 'include' }),
    );
  });

  it('getAppointmentList throws on non-ok response', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(null, { status: 500 }),
    );

    await expect(getAppointmentList()).rejects.toThrow(/Failed to load/);
  });
});
