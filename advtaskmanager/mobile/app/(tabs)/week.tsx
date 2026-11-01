import { useRouter } from 'expo-router';
import { useState } from 'react';
import { ActivityIndicator, Pressable, RefreshControl, ScrollView, StyleSheet, View } from 'react-native';
import { MainTopBar, Drawer, ListsSheet } from '@/components/MainNavigation';
import { BottomBar, palette } from '@/components/TaskPresentation';
import { Text } from '@/components/Themed';
import { useWeekTasks } from '@/hooks/useWeekTasks';
import { Task } from '@/api/models';

function monday(date: Date) { const result = new Date(date); const day = (result.getDay() + 6) % 7; result.setDate(result.getDate() - day); result.setHours(0, 0, 0, 0); return result; }
function dateKey(date: Date) { return date.toISOString().slice(0, 10); }

export default function WeekScreen() {
  const router = useRouter(); const query = useWeekTasks(); const [drawer, setDrawer] = useState(false); const [lists, setLists] = useState(false); const [search, setSearch] = useState('');
  const start = monday(new Date()); const rows = Array.from({ length: 7 }, (_, index) => { const date = new Date(start); date.setDate(start.getDate() + index); const key = dateKey(date); return { date, key, tasks: (query.data ?? []).filter((task) => dateKey(new Date(task.dueDate)) === key) }; });
  return <View style={styles.screen}><ScrollView contentContainerStyle={styles.content} refreshControl={<RefreshControl refreshing={query.isRefetching} onRefresh={() => void query.refetch()} tintColor={palette.text} />}><MainTopBar active="week" onDrawer={() => setDrawer(true)} onLists={() => setLists(true)} /> <Text style={styles.title}>Week</Text>{query.isLoading ? <ActivityIndicator color={palette.text} /> : null}{rows.map((row) => <Pressable key={row.key} onPress={() => router.push({ pathname: '/(tabs)/day', params: { date: row.key } })} style={styles.dayRow}><View style={[styles.date, row.key === dateKey(new Date()) && styles.today]}><Text style={styles.weekday}>{row.date.toLocaleDateString('en-GB', { weekday: 'short' })}</Text><Text style={styles.number}>{row.date.getDate()}</Text></View><View style={styles.middle}><Text style={styles.task}>{row.tasks[0]?.name ?? 'Nothing planned'}</Text>{row.tasks.length > 1 ? <Text style={styles.more}>+{row.tasks.length - 1} more</Text> : null}</View><Text style={styles.chevron}>›</Text></Pressable>)}{!query.isLoading && !query.data?.length ? <Text style={styles.empty}>Nothing planned for this week.</Text> : null}</ScrollView><BottomBar query={search} onQueryChange={setSearch} onCreate={() => router.push('/task/new')} /><Drawer visible={drawer} onClose={() => setDrawer(false)} /><ListsSheet visible={lists} onClose={() => setLists(false)} /></View>;
}
const styles = StyleSheet.create({ screen: { backgroundColor: palette.background, flex: 1 }, content: { gap: 8, padding: 18, paddingBottom: 112, paddingTop: 44 }, title: { color: palette.text, fontSize: 32, fontWeight: '500', marginVertical: 16 }, dayRow: { alignItems: 'center', backgroundColor: palette.group, borderColor: palette.border, borderRadius: 14, borderWidth: 1, flexDirection: 'row', minHeight: 72, padding: 12 }, date: { alignItems: 'center', borderRadius: 10, padding: 6, width: 58 }, today: { backgroundColor: '#3a3a3c' }, weekday: { color: palette.secondary, fontSize: 12 }, number: { color: palette.text, fontSize: 20, fontWeight: '500' }, middle: { flex: 1, gap: 4 }, task: { color: palette.text, fontSize: 15 }, more: { color: palette.secondary, fontSize: 12 }, chevron: { color: palette.secondary, fontSize: 26 }, empty: { color: palette.secondary, padding: 30, textAlign: 'center' } });
