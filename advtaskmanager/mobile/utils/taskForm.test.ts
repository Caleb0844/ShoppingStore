import { formatLocalDateTime, formatDateLabel, formatTimeLabel } from './dates';
import { taskFormSchema } from '../tasks/schemas';

describe('task form formatting and validation', () => {
  const date = new Date('2026-11-02T09:30:00+03:00');
  it('formats Nairobi labels and backend LocalDateTime JSON', () => {
    expect(formatDateLabel(date)).toBe('Mon, 2 Nov 2026');
    expect(formatTimeLabel(date)).toBe('09:30');
    expect(formatLocalDateTime(date)).toBe('2026-11-02T09:30:00');
  });
  it('requires end time after start time', () => {
    const form = {
      name: 'Task', description: 'Description', priority: 'NOT_SET',
      dueDate: '2026-11-02T06:30:00', startsAt: '2026-11-02T09:00:00',
      endsAt: '2026-11-02T08:00:00', repetition: 'NONE', reminder: 'NONE',
    };
    expect(taskFormSchema.safeParse(form).success).toBe(false);
  });
});
