import { passwordChangeSchema } from './schemas';

describe('password change schema', () => {
  it('requires a current password and an eight-character new password', () => {
    expect(passwordChangeSchema.safeParse({ currentPassword: '', newPassword: 'short', confirmPassword: 'short' }).success).toBe(false);
  });

  it('requires matching new passwords', () => {
    expect(passwordChangeSchema.safeParse({ currentPassword: 'current', newPassword: 'long-enough', confirmPassword: 'different' }).success).toBe(false);
  });

  it('accepts valid password changes', () => {
    expect(passwordChangeSchema.safeParse({ currentPassword: 'current', newPassword: 'long-enough', confirmPassword: 'long-enough' }).success).toBe(true);
  });
});
