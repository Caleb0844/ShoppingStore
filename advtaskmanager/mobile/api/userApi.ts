import { request } from './httpClient';
import { PasswordChangeRequest, User } from './models';

export const userApi = {
  create: (user: Partial<User> & { password: string }) =>
    request<User>('/api/users', {
      method: 'POST',
      body: JSON.stringify(user),
    }),
  list: () => request<User[]>('/api/users'),
  me: () => request<User>('/api/users/me'),
  get: (id: number) => request<User>(`/api/users/${id}`),
  update: (id: number, user: Partial<User>) =>
    request<User>(`/api/users/${id}`, {
      method: 'PUT',
      body: JSON.stringify(user),
    }),
  changePassword: (body: PasswordChangeRequest) =>
    request<User>('/api/users/me/password', {
      method: 'PUT',
      body: JSON.stringify(body),
    }),
  remove: (id: number) => request<void>(`/api/users/${id}`, { method: 'DELETE' }),
};
