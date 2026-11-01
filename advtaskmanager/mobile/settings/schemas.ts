import { z } from 'zod';

export const passwordChangeSchema = z.object({
  currentPassword: z.string().min(1, 'Current password is required'),
  newPassword: z.string().min(8, 'New password must be at least 8 characters'),
  confirmPassword: z.string().min(1, 'Confirm your new password'),
}).refine((value) => value.newPassword === value.confirmPassword, {
  path: ['confirmPassword'],
  message: 'New passwords do not match',
});

export type PasswordChangeForm = z.infer<typeof passwordChangeSchema>;
