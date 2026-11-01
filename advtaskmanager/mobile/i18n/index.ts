const strings = {
  today: 'Today', week: 'Week', month: 'Month', overdue: 'Overdue',
  postponed: 'Postponed', cancelled: 'Cancelled', completed: 'Completed',
  search: 'Search', newTask: 'New task', loading: 'Loading…', retry: 'Try again',
  noTasks: 'Nothing here yet.', lists: 'Lists', settings: 'Settings',
  profile: 'Profile', signOut: 'Sign out', left: 'left', andMore: 'and more',
  tasks: 'tasks', laterToday: 'Later today',
};
export type TranslationKey = keyof typeof strings;
export const t = (key: TranslationKey) => strings[key];
