import { useQuery } from '@tanstack/react-query';
import { taskApi } from '../api/taskApi';

export function useWeekTasks() {
  return useQuery({ queryKey: ['tasks', 'week'], queryFn: taskApi.week });
}
