import { request } from './httpClient';
import { Task } from './models';

export interface TaskRange {
  from: string;
  to: string;
}

export const rangeApi = {
  list: ({ from, to }: TaskRange): Promise<Task[]> => {
    // TODO: switch to /api/tasks/range once the backend endpoint is deployed.
    return request<Task[]>('/api/tasks?status=ALL').then((tasks) =>
      tasks.filter((task) => task.dueDate.slice(0, 10) >= from && task.dueDate.slice(0, 10) <= to),
    );
  },
};
