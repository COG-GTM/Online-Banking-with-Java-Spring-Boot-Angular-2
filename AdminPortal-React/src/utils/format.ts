function pad(value: number): string {
  return value.toString().padStart(2, '0');
}

// Mirrors Angular's `date: 'MM/dd/yyyy'` pipe.
export function formatDate(value: string | number | Date): string {
  if (value === null || value === undefined || value === '') {
    return '';
  }
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) {
    return '';
  }
  return `${pad(d.getMonth() + 1)}/${pad(d.getDate())}/${d.getFullYear()}`;
}

// Mirrors Angular's `date: 'MM/dd/yyyy - hh:mm'` pipe (12-hour clock).
export function formatDateTime(value: string | number | Date): string {
  if (value === null || value === undefined || value === '') {
    return '';
  }
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) {
    return '';
  }
  const hours12 = d.getHours() % 12 || 12;
  return `${formatDate(value)} - ${pad(hours12)}:${pad(d.getMinutes())}`;
}
