import * as SecureStore from 'expo-secure-store';
import { ApiError, normalizeApiError } from './errors';

const TOKEN_KEY = 'auth_token';
const API_BASE_URL = process.env.EXPO_PUBLIC_API_URL ?? 'http://localhost:8080';
type UnauthorizedHandler = () => void;

let unauthorizedHandler: UnauthorizedHandler | undefined;

export function setUnauthorizedHandler(handler: UnauthorizedHandler | undefined) {
  unauthorizedHandler = handler;
}

export async function setAuthToken(token: string): Promise<void> {
  await SecureStore.setItemAsync(TOKEN_KEY, token);
}

export async function clearAuthToken(): Promise<void> {
  await SecureStore.deleteItemAsync(TOKEN_KEY);
}

export async function getAuthToken(): Promise<string | null> {
  return SecureStore.getItemAsync(TOKEN_KEY);
}

async function parseResponseBody(response: Response): Promise<unknown> {
  if (response.status === 204) return undefined;
  const text = await response.text();
  if (!text) return undefined;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

export async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = await getAuthToken();
  const headers = new Headers(init.headers);
  headers.set('Accept', 'application/json');
  if (init.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, { ...init, headers });
  } catch (error) {
    throw new ApiError(0, 'Unable to reach the server.', {
      error: error instanceof Error ? error.message : undefined,
    });
  }

  const body = await parseResponseBody(response);
  if (!response.ok) {
    const error = normalizeApiError(response.status, body);
    if (response.status === 401) {
      await clearAuthToken();
      unauthorizedHandler?.();
    }
    throw error;
  }
  return body as T;
}
