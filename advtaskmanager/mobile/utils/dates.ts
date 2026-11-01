const NAIROBI_TIME_ZONE = 'Africa/Nairobi';

function dateParts(date: Date): { year: number; month: number; day: number } {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: NAIROBI_TIME_ZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).formatToParts(date);
  return {
    year: Number(parts.find((part) => part.type === 'year')?.value),
    month: Number(parts.find((part) => part.type === 'month')?.value),
    day: Number(parts.find((part) => part.type === 'day')?.value),
  };
}

export function formatRangeDate(date: Date): string {
  const parts = dateParts(date);
  return `${parts.year.toString().padStart(4, '0')}-${parts.month
    .toString()
    .padStart(2, '0')}-${parts.day.toString().padStart(2, '0')}`;
}

export function monthRange(year: number, month: number): { from: string; to: string } {
  const first = new Date(Date.UTC(year, month, 1));
  const last = new Date(Date.UTC(year, month + 1, 0));
  return { from: formatRangeDate(first), to: formatRangeDate(last) };
}

export function monthGrid(year: number, month: number): Date[] {
  const firstDay = new Date(Date.UTC(year, month, 1));
  const mondayOffset = (firstDay.getUTCDay() + 6) % 7;
  const firstCell = new Date(Date.UTC(year, month, 1 - mondayOffset));
  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(firstCell);
    date.setUTCDate(firstCell.getUTCDate() + index);
    return date;
  });
}

export function taskDateKey(value: string): string {
  return value.slice(0, 10);
}

export function formatDateLabel(value: Date): string {
  return new Intl.DateTimeFormat('en-GB', {
    timeZone: NAIROBI_TIME_ZONE,
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  }).format(value);
}

export function formatTimeLabel(value: Date): string {
  const formatted = new Intl.DateTimeFormat('en-GB', {
    timeZone: NAIROBI_TIME_ZONE,
    hour: 'numeric',
    minute: '2-digit',
  }).format(value);
  return formatted.replace(/^(\d):/, '0$1:');
}

export function formatLocalDateTime(value: Date): string {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: NAIROBI_TIME_ZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hourCycle: 'h23',
  }).formatToParts(value);
  const get = (type: string) => parts.find((part) => part.type === type)?.value ?? '';
  return `${get('year')}-${get('month')}-${get('day')}T${get('hour')}:${get('minute')}:00`;
}
