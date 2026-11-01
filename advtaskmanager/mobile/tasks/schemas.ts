import { z } from 'zod';

export const taskFormSchema = z.object({
  name: z.string().trim().min(1, 'Name is required').max(255, 'Name must be at most 255 characters'),
  description: z.string().trim().min(1, 'Description is required').max(255, 'Description must be at most 255 characters'),
  priority: z.string().optional(),
  dueDate: z.string().trim().min(1, 'Due date is required'),
  startsAt: z.string().trim().min(1, 'Start time is required'),
  endsAt: z.string().optional(),
  repetition: z.enum(['NONE', 'DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY']),
  reminder: z.enum(['NONE', 'MINUTES_5', 'MINUTES_10', 'MINUTES_30', 'HOURS_1', 'HOURS_3', 'CUSTOM']),
  reminderAmount: z.string().optional(),
  reminderUnit: z.enum(['MINUTES', 'HOURS', 'DAYS', 'WEEKS']).optional(),
}).superRefine((value, context) => {
  if (value.endsAt && value.endsAt <= value.startsAt) {
    context.addIssue({ code: 'custom', path: ['endsAt'], message: 'End time must be after start time' });
  }
  if (value.reminder === 'CUSTOM') {
    const amount = Number(value.reminderAmount);
    if (!Number.isInteger(amount) || amount <= 0) {
      context.addIssue({ code: 'custom', path: ['reminderAmount'], message: 'Custom amount must be positive' });
    }
    if (!value.reminderUnit) {
      context.addIssue({ code: 'custom', path: ['reminderUnit'], message: 'Custom reminder unit is required' });
    }
  }
});

export type TaskForm = z.infer<typeof taskFormSchema>;
