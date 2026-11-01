import { useQuery } from '@tanstack/react-query';
import { rangeApi } from '../api/rangeApi';
import { Task } from '../api/models';
import { monthRange, taskDateKey } from '../utils/dates';

export function useMonthTasks(year: number, month: number) {
  const range = monthRange(year, month);
  const query = useQuery({
    queryKey: ['tasks', 'range', range.from, range.to],
    queryFn: () => rangeApi.list(range),
  });
  const byDate = new Map<string, Task[]>();
  (query.data ?? []).forEach((task) => {
    const key = taskDateKey(task.dueDate);
    byDate.set(key, [...(byDate.get(key) ?? []), task]);
  });
  return { ...query, tasksByDate: byDate, range };
}
