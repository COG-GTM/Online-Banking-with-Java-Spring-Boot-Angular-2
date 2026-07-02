import { describe, expect, it } from 'vitest';
import { formatAppointmentDate } from './date';

describe('formatAppointmentDate', () => {
  it('formats a date as MM/dd/yyyy - hh:mm (12-hour)', () => {
    expect(formatAppointmentDate('2024-03-15T14:30:00')).toBe(
      '03/15/2024 - 02:30',
    );
  });

  it('formats midnight as 12 on a 12-hour clock', () => {
    expect(formatAppointmentDate('2024-01-01T00:05:00')).toBe(
      '01/01/2024 - 12:05',
    );
  });

  it('returns empty string for invalid input', () => {
    expect(formatAppointmentDate('not-a-date')).toBe('');
  });
});
