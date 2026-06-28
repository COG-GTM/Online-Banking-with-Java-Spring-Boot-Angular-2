function pad(value: number): string {
  return value.toString().padStart(2, '0');
}

/**
 * Mirrors the Angular template's `date: 'MM/dd/yyyy - hh:mm'` pipe.
 * Uses 12-hour clock to match the original `hh` token.
 */
export function formatAppointmentDate(value: string | number | Date): string {
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) {
    return '';
  }
  const month = pad(d.getMonth() + 1);
  const day = pad(d.getDate());
  const year = d.getFullYear();
  const hours12 = d.getHours() % 12 === 0 ? 12 : d.getHours() % 12;
  const hours = pad(hours12);
  const minutes = pad(d.getMinutes());
  return `${month}/${day}/${year} - ${hours}:${minutes}`;
}
