import { ApiError, normalizeApiError } from './errors';

describe('normalizeApiError', () => {
  it('preserves the documented validation error body', () => {
    const error = normalizeApiError(400, {
      status: 400,
      message: 'Please correct the invalid fields',
      path: '/api/tasks/1',
      fieldErrors: { name: 'Task name is required' },
    });

    expect(error).toBeInstanceOf(ApiError);
    expect(error.status).toBe(400);
    expect(error.message).toBe('Please correct the invalid fields');
    expect(error.path).toBe('/api/tasks/1');
    expect(error.fieldErrors).toEqual({ name: 'Task name is required' });
  });

  it.each<number>([401, 403, 404, 409, 429, 500])(
    'normalizes status %s with a usable fallback',
    (status) => {
      const error = normalizeApiError(status, {});
      expect(error.status).toBe(status);
      expect(error.message).toBeTruthy();
    },
  );

  it('preserves overlap confirmation details', () => {
    const error = normalizeApiError(409, {
      message: 'Task overlaps another task',
      confirmationRequired: true,
      conflictingTasks: [
        { taskId: 7, name: 'Existing task', startsAt: '2026-01-01', endsAt: '2026-01-02' },
      ],
    });

    expect(error.confirmationRequired).toBe(true);
    expect(error.conflictingTasks?.[0].taskId).toBe(7);
  });
});
