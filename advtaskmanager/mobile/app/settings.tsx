import { useRouter } from 'expo-router';
import { useEffect, useMemo, useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, TextInput, View } from 'react-native';
import { useQuery } from '@tanstack/react-query';
import { ApiError } from '../api/errors';
import { Reminder, Task, User } from '../api/models';
import { taskApi } from '../api/taskApi';
import { userApi } from '../api/userApi';
import { useAuth } from '../auth/AuthContext';
import { useTaskOptions } from '../hooks/useTaskOptions';
import { getDefaultReminder, getLanguage, getTheme, setDefaultReminder, setLanguage, setTheme, AppLanguage, AppTheme } from '../settings/preferences';
import { passwordChangeSchema, PasswordChangeForm } from '../settings/schemas';
import { Text } from '../components/Themed';

const fallbackReminders: Reminder[] = ['NONE', 'MINUTES_5', 'MINUTES_10', 'MINUTES_30', 'HOURS_1', 'HOURS_3', 'CUSTOM'];

export default function SettingsScreen() {
  const router = useRouter();
  const { user, signOut } = useAuth();
  const { data: options } = useTaskOptions();
  const tasksQuery = useQuery({ queryKey: ['tasks', 'settings-counts'], queryFn: () => taskApi.list() });
  const [language, setLanguageState] = useState<AppLanguage>('en');
  const [theme, setThemeState] = useState<AppTheme>('dark');
  const [defaultReminder, setDefaultReminderState] = useState<Reminder>('NONE');
  const [passwords, setPasswords] = useState<PasswordChangeForm>({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [passwordError, setPasswordError] = useState('');
  const [passwordSaved, setPasswordSaved] = useState('');
  const [savingPassword, setSavingPassword] = useState(false);

  useEffect(() => {
    void Promise.all([getLanguage(), getTheme(), getDefaultReminder()]).then(([savedLanguage, savedTheme, savedReminder]) => {
      setLanguageState(savedLanguage);
      setThemeState(savedTheme);
      setDefaultReminderState(savedReminder);
    });
  }, []);

  const reminderValues = options?.reminders.map((choice) => choice.value as Reminder) ?? fallbackReminders;
  const counts = useMemo(() => getCounts(tasksQuery.data ?? user?.tasks ?? []), [tasksQuery.data, user?.tasks]);

  async function changePassword() {
    const result = passwordChangeSchema.safeParse(passwords);
    if (!result.success) {
      setPasswordError(result.error.issues[0]?.message ?? 'Check the password fields.');
      setPasswordSaved('');
      return;
    }
    setSavingPassword(true);
    setPasswordError('');
    setPasswordSaved('');
    try {
      await userApi.changePassword({ currentPassword: result.data.currentPassword, newPassword: result.data.newPassword });
      setPasswords({ currentPassword: '', newPassword: '', confirmPassword: '' });
      setPasswordSaved('Password changed successfully.');
    } catch (error) {
      if (error instanceof ApiError && (error.status === 400 || error.status === 401)) {
        setPasswordError('The current password is incorrect.');
      } else {
        setPasswordError(error instanceof Error ? error.message : 'Unable to change password.');
      }
    } finally {
      setSavingPassword(false);
    }
  }

  return (
    <ScrollView style={styles.screen} contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
      <Pressable onPress={() => router.back()}><Text style={styles.back}>‹ Back</Text></Pressable>
      <Text style={styles.title}>Settings</Text>
      <Section title="Preferences">
        <Text style={styles.label}>Language</Text>
        <Choice value={language === 'en' ? 'English' : language} selected onPress={() => void setLanguage('en').then(() => setLanguageState('en'))} />
        <Text style={styles.label}>Theme</Text>
        <Choice value="Dark" selected={theme === 'dark'} onPress={() => void setTheme('dark').then(() => setThemeState('dark'))} />
        <Text style={styles.label}>Default reminder</Text>
        <View style={styles.choices}>{reminderValues.map((value) => <Choice key={value} value={value.replaceAll('_', ' ')} selected={defaultReminder === value} onPress={() => void setDefaultReminder(value).then(() => setDefaultReminderState(value))} />)}</View>
      </Section>
      <Section title="Profile">
        <ProfileRow label="Name" value={user?.name ?? ''} />
        <ProfileRow label="Username" value={user?.username ?? ''} />
        <ProfileRow label="Email" value={user?.email ?? ''} />
        <View style={styles.counts}><ProfileRow label="Total tasks" value={String(counts.total)} /><ProfileRow label="Completed" value={String(counts.completed)} /><ProfileRow label="Pending" value={String(counts.pending)} /></View>
      </Section>
      <Section title="Change password">
        <PasswordField placeholder="Current password" value={passwords.currentPassword} onChangeText={(value) => setPasswords((current) => ({ ...current, currentPassword: value }))} />
        <PasswordField placeholder="New password (8+ characters)" value={passwords.newPassword} onChangeText={(value) => setPasswords((current) => ({ ...current, newPassword: value }))} />
        <PasswordField placeholder="Confirm new password" value={passwords.confirmPassword} onChangeText={(value) => setPasswords((current) => ({ ...current, confirmPassword: value }))} />
        {passwordError ? <Text style={styles.error}>{passwordError}</Text> : null}
        {passwordSaved ? <Text style={styles.success}>{passwordSaved}</Text> : null}
        <Pressable disabled={savingPassword} onPress={() => void changePassword()} style={styles.primary}><Text style={styles.primaryText}>{savingPassword ? 'Changing…' : 'Change password'}</Text></Pressable>
      </Section>
      <Pressable onPress={() => void signOut()} style={styles.signOut}><Text style={styles.signOutText}>Sign out</Text></Pressable>
    </ScrollView>
  );
}

function getCounts(tasks: Task[]) {
  return {
    total: tasks.length,
    completed: tasks.filter((task) => task.status === 'COMPLETED' || task.completed).length,
    pending: tasks.filter((task) => task.status !== 'COMPLETED' && !task.completed).length,
  };
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return <View style={styles.section}><Text style={styles.sectionTitle}>{title}</Text>{children}</View>;
}

function Choice({ value, selected, onPress }: { value: string; selected: boolean; onPress: () => void }) {
  return <Pressable onPress={onPress} style={[styles.choice, selected && styles.selectedChoice]}><Text style={[styles.choiceText, selected && styles.selectedChoiceText]}>{value}</Text></Pressable>;
}

function ProfileRow({ label, value }: { label: string; value: string }) {
  return <View style={styles.profileRow}><Text style={styles.label}>{label}</Text><Text style={styles.value}>{value}</Text></View>;
}

function PasswordField(props: { placeholder: string; value: string; onChangeText: (value: string) => void }) {
  return <TextInput secureTextEntry placeholder={props.placeholder} placeholderTextColor="#77758c" value={props.value} onChangeText={props.onChangeText} style={styles.input} />;
}

const styles = StyleSheet.create({
  screen: { backgroundColor: '#0f1020', flex: 1 },
  content: { gap: 18, padding: 22, paddingBottom: 48, paddingTop: 58 },
  back: { color: '#c4b5fd', fontSize: 16, fontWeight: '700' },
  title: { color: '#f8f7ff', fontSize: 38, fontWeight: '800' },
  section: { backgroundColor: '#1b1b31', borderRadius: 16, gap: 12, padding: 16 },
  sectionTitle: { color: '#f8f7ff', fontSize: 18, fontWeight: '800', marginBottom: 3 },
  label: { color: '#77758c', fontSize: 12, fontWeight: '700', textTransform: 'uppercase' },
  choices: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  choice: { backgroundColor: '#272641', borderRadius: 18, paddingHorizontal: 12, paddingVertical: 9 },
  selectedChoice: { backgroundColor: '#8b5cf6' },
  choiceText: { color: '#aaa7bd', fontWeight: '700', textTransform: 'capitalize' },
  selectedChoiceText: { color: '#fff' },
  profileRow: { gap: 4, paddingVertical: 4 },
  value: { color: '#f8f7ff', fontSize: 16 },
  counts: { borderTopColor: '#302f4a', borderTopWidth: 1, flexDirection: 'row', justifyContent: 'space-between', paddingTop: 10 },
  input: { backgroundColor: '#272641', borderColor: '#302f4a', borderRadius: 10, borderWidth: 1, color: '#f8f7ff', padding: 13 },
  primary: { alignItems: 'center', backgroundColor: '#8b5cf6', borderRadius: 11, padding: 14 },
  primaryText: { color: '#fff', fontWeight: '800' },
  error: { color: '#fb7185', lineHeight: 20 },
  success: { color: '#86efac' },
  signOut: { alignItems: 'center', borderColor: '#7f304b', borderRadius: 11, borderWidth: 1, padding: 14 },
  signOutText: { color: '#fb7185', fontWeight: '800' },
});
