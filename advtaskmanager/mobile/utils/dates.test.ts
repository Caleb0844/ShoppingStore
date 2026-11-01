import { formatRangeDate, monthGrid, monthRange } from './dates';

describe('date utilities', () => {
  it('formats dates for the Africa/Nairobi calendar day', () => {
    expect(formatRangeDate(new Date('2026-10-30T00:00:00.000Z'))).toBe('2026-10-30');
    expect(formatRangeDate(new Date('2026-10-30T21:00:00.000Z'))).toBe('2026-10-31');
  });

  it('returns the inclusive calendar month range', () => {
    expect(monthRange(2026, 9)).toEqual({ from: '2026-10-01', to: '2026-10-31' });
  });

  it('starts the grid on Monday and always returns 42 cells', () => {
    const grid = monthGrid(2026, 9);
    expect(grid).toHaveLength(42);
    expect(grid[0].toISOString().slice(0, 10)).toBe('2026-09-28');
    expect(grid[grid.length - 1].toISOString().slice(0, 10)).toBe('2026-11-08');
  });
});
