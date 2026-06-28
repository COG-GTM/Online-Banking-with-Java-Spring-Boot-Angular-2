import { describe, expect, it } from 'vitest';
import { formatDate } from './format';

describe('formatDate', () => {
  it('formats an ISO date string as MM/dd/yyyy', () => {
    expect(formatDate('2023-01-09T00:00:00')).toBe('01/09/2023');
  });

  it('formats an epoch millisecond timestamp', () => {
    expect(formatDate(new Date(2024, 11, 25).getTime())).toBe('12/25/2024');
  });

  it('returns an empty string for nullish or invalid values', () => {
    expect(formatDate(null)).toBe('');
    expect(formatDate(undefined)).toBe('');
    expect(formatDate('')).toBe('');
    expect(formatDate('not-a-date')).toBe('');
  });
});
