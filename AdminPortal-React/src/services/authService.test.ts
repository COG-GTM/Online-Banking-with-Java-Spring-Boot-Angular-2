import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { sendCredential, logout } from './authService';
import { API_BASE_URL } from './config';

describe('authService', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it('sendCredential POSTs urlencoded credentials with credentials included', async () => {
    const fetchMock = vi.mocked(fetch);
    fetchMock.mockResolvedValue(new Response(null, { status: 200 }));

    await sendCredential('alice', 'secret');

    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe(`${API_BASE_URL}/index`);
    expect(init?.method).toBe('POST');
    expect(init?.credentials).toBe('include');
    expect(
      (init?.headers as Record<string, string>)['Content-Type'],
    ).toBe('application/x-www-form-urlencoded');
    expect(init?.body).toBe('username=alice&password=secret');
  });

  it('sendCredential throws when the response is not ok', async () => {
    vi.mocked(fetch).mockResolvedValue(new Response(null, { status: 401 }));
    await expect(sendCredential('alice', 'wrong')).rejects.toThrow(
      /401/,
    );
  });

  it('logout GETs the logout endpoint with credentials included', async () => {
    const fetchMock = vi.mocked(fetch);
    fetchMock.mockResolvedValue(new Response(null, { status: 200 }));

    await logout();

    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe(`${API_BASE_URL}/logout`);
    expect(init?.method).toBe('GET');
    expect(init?.credentials).toBe('include');
  });
});
