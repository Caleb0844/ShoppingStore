import { useRouter } from 'expo-router';
import { useState } from 'react';
import { ActivityIndicator, Pressable, RefreshControl, ScrollView, StyleSheet, View } from 'react-native';
import { useAuth } from '@/auth/AuthContext';
import { useTodayTasks } from '@/hooks/useTodayTasks';
import { ApiError } from '@/api/errors';
import { MainTopBar, Drawer, ListsSheet } from '@/components/MainNavigation';
import { BottomBar, palette, TaskGroup } from '@/components/TaskPresentation';
import { Text } from '@/components/Themed';
import { t } from '@/i18n';

export default function TodayScreen() {
  const router = useRouter(); const { user } = useAuth();
  const { today, overdue, isLoading, isError, error, isRefreshing, refresh, toggleCompleted } = useTodayTasks();
  const [drawer, setDrawer] = useState(false); const [lists, setLists] = useState(false); const [search, setSearch] = useState('');
  const filtered = today.filter((task) => `${task.name} ${task.description}`.toLowerCase().includes(search.toLowerCase()));
  return <View style={styles.screen}><ScrollView contentContainerStyle={styles.content} refreshControl={<RefreshControl refreshing={isRefreshing} onRefresh={() => void refresh()} tintColor={palette.text} />}><MainTopBar active="today" onDrawer={() => setDrawer(true)} onLists={() => setLists(true)} /><Text style={styles.title}>{t('today')}</Text><Text style={styles.subtitle}>{new Date().toLocaleDateString('en-GB', { weekday: 'long', day: 'numeric', month: 'long' })} · {today.filter((x) => !x.completed).length} {t('left')}</Text>{overdue.length ? <Pressable onPress={() => router.push({ pathname: '/(tabs)/all', params: { overdue: 'true' } })} style={styles.overdue}><Text style={styles.overdueTitle}>{overdue.length} {t('overdue').toLowerCase()}</Text><Text style={styles.overdueText}>{overdue[0].name} {overdue.length > 1 ? `and ${overdue.length - 1} ${t('andMore')}` : ''}</Text><Text style={styles.chevron}>›</Text></Pressable> : null}{isLoading ? <State text={t('loading')} loading /> : null}{isError ? <State text={error instanceof ApiError ? error.message : 'Unable to load tasks.'} retry={() => void refresh()} /> : null}{!isLoading && !isError ? <TaskGroup tasks={filtered} onToggle={toggleCompleted} onPress={(task) => router.push(`/task/view/${task.id}`)} /> : null}{!isLoading && !isError && !filtered.length ? <Text style={styles.empty}>{t('noTasks')}</Text> : null}</ScrollView><BottomBar query={search} onQueryChange={setSearch} onCreate={() => router.push('/task/new')} /><Drawer visible={drawer} onClose={() => setDrawer(false)} /><ListsSheet visible={lists} onClose={() => setLists(false)} /></View>;
}
function State({ text, loading, retry }: { text: string; loading?: boolean; retry?: () => void }) { return <View style={styles.state}>{loading ? <ActivityIndicator color={palette.text} /> : null}<Text style={styles.subtitle}>{text}</Text>{retry ? <Pressable onPress={retry}><Text style={styles.retry}>{t('retry')}</Text></Pressable> : null}</View>; }
const styles = StyleSheet.create({ screen: { backgroundColor: palette.background, flex: 1 }, content: { gap: 12, padding: 18, paddingBottom: 112, paddingTop: 44 }, title: { color: palette.text, fontSize: 32, fontWeight: '500', marginTop: 20 }, subtitle: { color: palette.secondary, fontSize: 14 }, overdue: { backgroundColor: '#2a1515', borderColor: palette.red, borderRadius: 14, borderWidth: 1, gap: 4, padding: 14, position: 'relative' }, overdueTitle: { color: palette.red, fontWeight: '500' }, overdueText: { color: palette.text }, chevron: { color: palette.red, fontSize: 25, position: 'absolute', right: 15, top: 17 }, state: { alignItems: 'center', gap: 10, paddingVertical: 80 }, retry: { color: palette.text, fontWeight: '500' }, empty: { color: palette.secondary, paddingVertical: 45, textAlign: 'center' } });
