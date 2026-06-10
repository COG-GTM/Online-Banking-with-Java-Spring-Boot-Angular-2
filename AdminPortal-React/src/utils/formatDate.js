function pad(value) {
  return String(value).padStart(2, '0');
}

// Formats a date as MM/dd/yyyy (mirrors the Angular `date: 'MM/dd/yyyy'` pipe).
export function formatDate(value) {
  if (!value) {
    return '';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '';
  }
  return `${pad(date.getMonth() + 1)}/${pad(date.getDate())}/${date.getFullYear()}`;
}

// Formats a date as MM/dd/yyyy - hh:mm (mirrors `date: 'MM/dd/yyyy - hh:mm'`).
export function formatDateTime(value) {
  if (!value) {
    return '';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '';
  }
  const hours12 = date.getHours() % 12 || 12;
  return `${pad(date.getMonth() + 1)}/${pad(date.getDate())}/${date.getFullYear()} - ${pad(
    hours12
  )}:${pad(date.getMinutes())}`;
}
