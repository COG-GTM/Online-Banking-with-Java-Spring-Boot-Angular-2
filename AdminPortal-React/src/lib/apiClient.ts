// Typed fetch wrapper. Single place for the credentials mode (session cookie),
// base URL and error mapping. In dev, Vite proxies the backend paths to :8080,
// so requests stay same-origin and the cookie flows through unchanged.

export class ApiError extends Error {
  status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

const BASE_URL = ''

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(BASE_URL + path, {
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...options.headers,
    },
    ...options,
  })

  if (!response.ok) {
    throw new ApiError(response.status, await readErrorMessage(response))
  }

  if (response.status === 204) {
    return undefined as T
  }

  const text = await response.text()
  return (text ? JSON.parse(text) : undefined) as T
}

async function readErrorMessage(response: Response): Promise<string> {
  try {
    const text = await response.text()
    if (!text) {
      return response.statusText
    }
    const data = JSON.parse(text) as { message?: string }
    return data.message ?? response.statusText
  } catch {
    return response.statusText
  }
}

export const apiClient = {
  get: <T>(path: string) => request<T>(path, { method: 'GET' }),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, {
      method: 'POST',
      body: body === undefined ? undefined : JSON.stringify(body),
    }),
  delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
}
