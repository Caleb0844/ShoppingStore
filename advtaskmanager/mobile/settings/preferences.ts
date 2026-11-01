import * as SecureStore from 'expo-secure-store';
import { Reminder } from '../api/models';

const LANGUAGE_KEY = 'app_language';
const THEME_KEY = 'app_theme';
const REMINDER_KEY = 'default_reminder';

export type AppLanguage = 'en';
export type AppTheme = 'dark';

export async function getLanguage(): Promise<AppLanguage> {
  return ((await SecureStore.getItemAsync(LANGUAGE_KEY)) as AppLanguage | null) ?? 'en';
}

export async function setLanguage(language: AppLanguage): Promise<void> {
  await SecureStore.setItemAsync(LANGUAGE_KEY, language);
}

export async function getTheme(): Promise<AppTheme> {
  return ((await SecureStore.getItemAsync(THEME_KEY)) as AppTheme | null) ?? 'dark';
}

export async function setTheme(theme: AppTheme): Promise<void> {
  await SecureStore.setItemAsync(THEME_KEY, theme);
}

export async function getDefaultReminder(): Promise<Reminder> {
  return ((await SecureStore.getItemAsync(REMINDER_KEY)) as Reminder | null) ?? 'NONE';
}

export async function setDefaultReminder(reminder: Reminder): Promise<void> {
  await SecureStore.setItemAsync(REMINDER_KEY, reminder);
}
