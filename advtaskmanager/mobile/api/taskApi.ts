import { request } from './httpClient';
import {
  NewTask,
  PostponeRequest,
  Task,
  TaskOptions,
  TaskInput,
  TaskUpdate,
  TaskView,
} from './models';

const query = (params: Record<string, string | number | boolean | undefined>) =>
  new URLSearchParams(
    Object.entries(params)
      .filter(([, value]) => value !== undefined)
      .map(([key, value]) => [key, String(value)]),
  ).toString();

export const taskApi = {
  list: (userId?: number, status: TaskView = 'ALL') =>
    request<Task[]>(`/api/tasks?${query({ userId, status })}`),
  get: (id: number) => request<Task>(`/api/tasks/${id}`),
  create: (task: TaskInput, confirmOverlap = false) =>
    request<Task>(`/api/tasks?${query({ confirmOverlap })}`, {
      method: 'POST',
      body: JSON.stringify(task),
    }),
  update: (id: number, task: TaskUpdate, confirmOverlap = false) =>
    request<Task>(`/api/tasks/${id}?${query({ confirmOverlap })}`, {
      method: 'PUT',
      body: JSON.stringify(task),
    }),
  remove: (id: number) =>
    request<void>(`/api/tasks/${id}`, { method: 'DELETE' }),
  options: () => request<TaskOptions>('/api/tasks/options'),
  today: () => request<Task[]>('/api/tasks/today'),
  week: () => request<Task[]>('/api/tasks/week'),
  upcoming: (days = 7) =>
    request<Task[]>(`/api/tasks/upcoming?${query({ days })}`),
  overdue: () => request<Task[]>('/api/tasks/overdue'),
  postpone: (id: number, body: PostponeRequest, confirmOverlap = false) =>
    request<Task>(`/api/tasks/${id}/postpone?${query({ confirmOverlap })}`, {
      method: 'PUT',
      body: JSON.stringify(body),
    }),
};
