export type TaskStatus = 'PENDING' | 'POSTPONED' | 'COMPLETED';
export type TaskView = 'ALL' | 'PENDING' | 'POSTPONED' | 'COMPLETED';
export type Repetition = 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'NOT_SET';
export type Reminder =
  | 'NONE'
  | 'MINUTES_5'
  | 'MINUTES_10'
  | 'MINUTES_30'
  | 'HOURS_1'
  | 'HOURS_3'
  | 'CUSTOM';
export type ReminderUnit = 'MINUTES' | 'HOURS' | 'DAYS' | 'WEEKS';
export type PostponeOption =
  | 'TOMORROW'
  | 'MINUTES_5'
  | 'MINUTES_10'
  | 'MINUTES_30'
  | 'HOURS_1'
  | 'HOURS_2'
  | 'HOURS_3'
  | 'HOURS_6'
  | 'HOURS_12'
  | 'DAYS_1';

export interface User {
  id: number;
  name: string;
  username: string;
  email: string;
  password?: string;
  tasks?: Task[];
  createdAt: string;
  updatedAt: string;
}

export interface Task {
  id: number;
  user: User;
  name: string;
  description: string;
  priority: Priority;
  status: TaskStatus;
  completed: boolean;
  reminder: Reminder;
  reminderAmount?: number;
  reminderUnit?: ReminderUnit;
  remindAt?: string;
  createdAt: string;
  updatedAt: string;
  dueDate: string;
  startsAt?: string;
  endsAt?: string;
  repetition: Repetition;
  nextOccurrenceAt?: string;
  sourceTaskId?: number;
}

export type NewTask = Omit<
  Task,
  | 'id'
  | 'user'
  | 'completed'
  | 'createdAt'
  | 'updatedAt'
  | 'remindAt'
  | 'nextOccurrenceAt'
  | 'sourceTaskId'
> & {
  user?: Pick<User, 'id'>;
  completed?: boolean;
};

export type TaskUpdate = Partial<
  Pick<
    Task,
    | 'name'
    | 'description'
    | 'priority'
    | 'status'
    | 'completed'
    | 'reminder'
    | 'reminderAmount'
    | 'reminderUnit'
    | 'dueDate'
    | 'startsAt'
    | 'endsAt'
    | 'repetition'
  >
> & { user?: Pick<User, 'id'> };

export interface TaskInput {
  name: string;
  description: string;
  priority: Priority;
  status?: TaskStatus;
  dueDate: string;
  startsAt?: string;
  endsAt?: string;
  repetition: Repetition;
  reminder: Reminder;
  reminderAmount?: number;
  reminderUnit?: ReminderUnit;
}

export interface Choice {
  value: string;
  label: string;
}

export interface TaskOptions {
  statuses: TaskStatus[];
  views: TaskView[];
  repetitions: Repetition[];
  priorities: Priority[];
  reminders: Choice[];
  reminderUnits: ReminderUnit[];
  postponeOptions: Choice[];
}

export interface PostponeRequest {
  option: PostponeOption;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface TokenResponse {
  token: string;
  tokenType: string;
}

export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
}
