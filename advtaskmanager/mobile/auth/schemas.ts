import { z } from 'zod';

export const loginSchema = z.object({
  username: z.string().trim().min(1, 'Username is required'),
  password: z.string().min(1, 'Password is required'),
});

export const registerSchema = z.object({
  name: z.string().trim().min(1, 'Name is required').max(255, 'Name must be at most 255 characters'),
  username: z.string().trim().min(1, 'Username is required').max(255, 'Username must be at most 255 characters'),
  email: z.string().trim().email('Enter a valid email address').max(255, 'Email must be at most 255 characters'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
  confirmPassword: z.string().min(1, 'Confirm your password'),
}).refine((value) => value.password === value.confirmPassword, {
  path: ['confirmPassword'],
  message: 'Passwords do not match',
});

export type LoginForm = z.infer<typeof loginSchema>;
export type RegisterForm = z.infer<typeof registerSchema>;
