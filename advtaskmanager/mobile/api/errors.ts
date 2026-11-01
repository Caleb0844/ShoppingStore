export interface ApiErrorBody {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  fieldErrors?: Record<string, string>;
  confirmationRequired?: boolean;
  conflictingTasks?: Array<{
    taskId: number;
    name: string;
    startsAt: string;
    endsAt: string;
  }>;
}

export class ApiError extends Error {
  readonly status: number;
  readonly code?: string;
  readonly path?: string;
  readonly fieldErrors: Record<string, string>;
  readonly confirmationRequired: boolean;
  readonly conflictingTasks: ApiErrorBody['conflictingTasks'];

  constructor(
    status: number,
    message: string,
    body: ApiErrorBody = {},
  ) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = body.error;
    this.path = body.path;
    this.fieldErrors = body.fieldErrors ?? {};
    this.confirmationRequired = body.confirmationRequired === true;
    this.conflictingTasks = body.conflictingTasks;
  }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}

export function normalizeApiError(status: number, body: unknown): ApiError {
  const parsed = isRecord(body) ? (body as ApiErrorBody) : {};
  const messages: Record<number, string> = {
    400: 'The request is invalid.',
    401: 'Your session has expired. Please sign in again.',
    403: 'You do not have permission to perform this action.',
    404: 'The requested resource was not found.',
    409: 'The request conflicts with existing data.',
    429: 'Too many requests. Please try again later.',
    500: 'The server encountered an unexpected error.',
  };
  return new ApiError(
    status,
    typeof parsed.message === 'string' && parsed.message.length > 0
      ? parsed.message
      : messages[status] ?? 'The request could not be completed.',
    parsed,
  );
}
