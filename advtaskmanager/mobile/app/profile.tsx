import { useRouter } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import { Pressable, ScrollView, StyleSheet, View } from 'react-native';
import { userApi } from '../api/userApi';
import { taskApi } from '../api/taskApi';
import { Text } from '../components/Themed';
import { palette } from '../components/TaskPresentation';

export default function ProfileScreen() {
  const router = useRouter();
  const profile = useQuery({ queryKey: ['user', 'me'], queryFn: userApi.me });
  const tasks = useQuery({ queryKey: ['tasks', 'profile'], queryFn: () => taskApi.list() });
  const all = tasks.data ?? [];
  return <ScrollView style={styles.screen} contentContainerStyle={styles.content}><Pressable onPress={() => router.back()}><Text style={styles.back}>‹ Back</Text></Pressable><Text style={styles.title}>Profile</Text>{profile.isLoading ? <Text style={styles.muted}>Loading…</Text> : <View style={styles.group}><Row label="Name" value={profile.data?.name ?? ''} /><Row label="Username" value={profile.data?.username ?? ''} /><Row label="Email" value={profile.data?.email ?? ''} /><Row label="Total tasks" value={String(all.length)} /><Row label="Completed" value={String(all.filter((task) => task.completed).length)} /></View>}<Pressable onPress={() => router.push('/change-password')} style={styles.action}><Text style={styles.actionText}>Change password</Text></Pressable></ScrollView>;
}
function Row({ label, value }: { label: string; value: string }) { return <View style={styles.row}><Text style={styles.label}>{label}</Text><Text style={styles.value}>{value}</Text></View>; }
const styles = StyleSheet.create({ screen: { backgroundColor: palette.background, flex: 1 }, content: { gap: 18, padding: 22, paddingTop: 54 }, back: { color: palette.text, fontSize: 16 }, title: { color: palette.text, fontSize: 32, fontWeight: '500' }, group: { backgroundColor: palette.group, borderRadius: 14, gap: 16, padding: 16 }, row: { gap: 4 }, label: { color: palette.secondary, fontSize: 12 }, value: { color: palette.text, fontSize: 16 }, muted: { color: palette.secondary }, action: { backgroundColor: palette.button, borderRadius: 24, padding: 15 }, actionText: { color: palette.text, textAlign: 'center' } });
