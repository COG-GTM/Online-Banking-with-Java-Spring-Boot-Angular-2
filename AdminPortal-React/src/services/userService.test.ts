import { afterEach, describe, expect, it, vi } from 'vitest';
import { getSavingsTransactionList } from './userService';

describe('getSavingsTransactionList', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('requests the savings transaction endpoint with credentials included', async () => {
    const data = [
      {
        id: 1,
        date: '2023-01-09T00:00:00',
        description: 'Deposit',
        type: 'Credit',
        status: 'Complete',
        amount: 100,
        availableBalance: 1100,
      },
    ];
    const fetchMock = vi
      .spyOn(globalThis, 'fetch')
      .mockResolvedValue(new Response(JSON.stringify(data), { status: 200 }));

    const result = await getSavingsTransactionList('alice');

    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, options] = fetchMock.mock.calls[0];
    expect(url).toBe(
      'http://localhost:8080/api/user/savings/transaction?username=alice',
    );
    expect(options).toMatchObject({ method: 'GET', credentials: 'include' });
    expect(result).toEqual(data);
  });

  it('throws when the response is not ok', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response('nope', { status: 500, statusText: 'Server Error' }),
    );

    await expect(getSavingsTransactionList('bob')).rejects.toThrow(
      /Failed to load savings transactions/,
    );
  });
});
