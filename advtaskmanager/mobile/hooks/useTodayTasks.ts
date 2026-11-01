import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { taskApi } from '../api/taskApi';
import { Task } from '../api/models';

const todayKey = ['tasks', 'today'];
const overdueKey = ['tasks', 'overdue'];

export function useTodayTasks() {
  const queryClient = useQueryClient();
  const today = useQuery({ queryKey: todayKey, queryFn: taskApi.today });
  const overdue = useQuery({ queryKey: overdueKey, queryFn: taskApi.overdue });

  const completion = useMutation({
    mutationFn: ({ id, completed }: { id: number; completed: boolean }) =>
      taskApi.update(id, { completed }),
    onMutate: async ({ id, completed }) => {
      await Promise.all([
        queryClient.cancelQueries({ queryKey: todayKey }),
        queryClient.cancelQueries({ queryKey: overdueKey }),
      ]);
      const previousToday = queryClient.getQueryData<Task[]>(todayKey);
      const previousOverdue = queryClient.getQueryData<Task[]>(overdueKey);
      const update = (tasks: Task[] | undefined) =>
        tasks?.map((task) => (task.id === id ? { ...task, completed } : task));
      queryClient.setQueryData(todayKey, update(previousToday));
      queryClient.setQueryData(overdueKey, update(previousOverdue));
      return { previousToday, previousOverdue };
    },
    onError: (_error, _variables, context) => {
      if (context) {
        queryClient.setQueryData(todayKey, context.previousToday);
        queryClient.setQueryData(overdueKey, context.previousOverdue);
      }
    },
    onSettled: () => {
      void queryClient.invalidateQueries({ queryKey: todayKey });
      void queryClient.invalidateQueries({ queryKey: overdueKey });
    },
  });

  return {
    today: today.data ?? [],
    overdue: overdue.data ?? [],
    isLoading: today.isLoading || overdue.isLoading,
    isError: today.isError || overdue.isError,
    error: today.error ?? overdue.error,
    isRefreshing: today.isRefetching || overdue.isRefetching,
    refresh: () => Promise.all([today.refetch(), overdue.refetch()]),
    toggleCompleted: (task: Task) =>
      completion.mutate({ id: task.id, completed: !task.completed }),
  };
}
