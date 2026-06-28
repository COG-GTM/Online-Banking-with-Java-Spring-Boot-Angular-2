/**
 * Port of Angular's `date: 'MM/dd/yyyy'` DatePipe usage in the
 * primary-transaction template.
 *
 * Returns an empty string for nullish/invalid input (matching Angular's
 * lenient rendering of missing dates).
 */
export function formatDate(value: string | number | Date | null | undefined): string {
  if (value === null || value === undefined || value === "") {
    return "";
  }

  const d = new Date(value);
  if (Number.isNaN(d.getTime())) {
    return "";
  }

  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  const yyyy = d.getFullYear();
  return `${mm}/${dd}/${yyyy}`;
}
