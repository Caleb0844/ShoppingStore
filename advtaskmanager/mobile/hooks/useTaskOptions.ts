import { useQuery } from '@tanstack/react-query';
import { taskApi } from '../api/taskApi';

export function useTaskOptions() {
  return useQuery({
    queryKey: ['task-options'],
    queryFn: taskApi.options,
    staleTime: 5 * 60 * 1000,
    gcTime: 30 * 60 * 1000,
  });
}
