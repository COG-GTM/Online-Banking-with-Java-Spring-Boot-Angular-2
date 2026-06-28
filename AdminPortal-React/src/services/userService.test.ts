import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { disableUser, enableUser, getUsers } from './userService';
import type { User } from '../types/user';

const sampleUsers: User[] = [
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
];

function mockFetchOnce(body: unknown, ok = true, status = 200) {
  const fetchMock = vi.fn().mockResolvedValue({
    ok,
    status,
    json: async () => body,
  } as Response);
  vi.stubGlobal('fetch', fetchMock);
  return fetchMock;
}

describe('userService', () => {
  beforeEach(() => {
    vi.unstubAllGlobals();
  });
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('getUsers requests /api/user/all with credentials and returns parsed users', async () => {
    const fetchMock = mockFetchOnce(sampleUsers);

    const users = await getUsers();

    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, options] = fetchMock.mock.calls[0];
    expect(url).toBe('http://localhost:8080/api/user/all');
    expect(options).toMatchObject({ credentials: 'include' });
    expect(users).toEqual(sampleUsers);
  });

  it('enableUser hits the enable endpoint with the username', async () => {
    const fetchMock = mockFetchOnce({});

    await enableUser('jdoe');

    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8080/api/user/jdoe/enable',
      expect.objectContaining({ credentials: 'include' }),
    );
  });

  it('disableUser hits the disable endpoint with the username', async () => {
    const fetchMock = mockFetchOnce({});

    await disableUser('jdoe');

    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8080/api/user/jdoe/disable',
      expect.objectContaining({ credentials: 'include' }),
    );
  });

  it('throws when the response is not ok', async () => {
    mockFetchOnce({}, false, 500);
    await expect(getUsers()).rejects.toThrow(/500/);
  });
});
