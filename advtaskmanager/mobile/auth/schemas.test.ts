import { loginSchema, registerSchema } from './schemas';

describe('auth schemas', () => {
  it('requires login credentials', () => {
    expect(loginSchema.safeParse({ username: '', password: '' }).success).toBe(false);
  });

  it('requires an eight-character password and matching confirmation', () => {
    expect(registerSchema.safeParse({
      name: 'A User', username: 'auser', email: 'user@example.com',
      password: 'short', confirmPassword: 'short',
    }).success).toBe(false);
    expect(registerSchema.safeParse({
      name: 'A User', username: 'auser', email: 'user@example.com',
      password: 'long-enough', confirmPassword: 'different',
    }).success).toBe(false);
  });

  it('accepts a valid registration', () => {
    expect(registerSchema.safeParse({
      name: 'A User', username: 'auser', email: 'user@example.com',
      password: 'long-enough', confirmPassword: 'long-enough',
    }).success).toBe(true);
  });
});
