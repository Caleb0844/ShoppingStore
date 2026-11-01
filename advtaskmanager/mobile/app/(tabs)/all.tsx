import { useLocalSearchParams, useRouter } from 'expo-router';
import { useMemo, useState } from 'react';
import { ActivityIndicator, Pressable, RefreshControl, ScrollView, StyleSheet, TextInput, View } from 'react-native';
import { useQuery } from '@tanstack/react-query';
import { TaskView } from '../../api/models';
import { taskApi } from '../../api/taskApi';
import { Text } from '../../components/Themed';

const filters: Array<{ label: string; value: TaskView }> = [
  { label: 'All', value: 'ALL' },
  { label: 'Pending', value: 'PENDING' },
  { label: 'Postponed', value: 'POSTPONED' },
  { label: 'Completed', value: 'COMPLETED' },
];

export default function AllTasksScreen() {
  const router = useRouter();
  const params = useLocalSearchParams<{ status?: string; overdue?: string }>();
  const [status, setStatus] = useState<TaskView>((params.status as TaskView) || 'ALL');
  const [search, setSearch] = useState('');
  const query = useQuery({ queryKey: ['tasks', 'all', status], queryFn: () => taskApi.list(undefined, status) });
  const tasks = useMemo(() => (query.data ?? []).filter((task) => {
    if (params.overdue === 'true' && new Date(task.dueDate).getTime() >= Date.now()) return false;
    return !search.trim() || `${task.name} ${task.description}`.toLowerCase().includes(search.toLowerCase());
  }), [params.overdue, query.data, search]);

  return (
    <View style={styles.screen}>
      <ScrollView contentContainerStyle={styles.content} refreshControl={<RefreshControl refreshing={query.isRefetching} onRefresh={() => void query.refetch()} tintColor="#a78bfa" />}>
        <Pressable onPress={() => router.back()}><Text style={styles.back}>‹ Back</Text></Pressable>
        <Text style={styles.title}>{status === 'COMPLETED' ? 'Completed' : params.overdue === 'true' ? 'Overdue' : status === 'ALL' ? 'All tasks' : status}</Text>
        <TextInput value={search} onChangeText={setSearch} placeholder="Search name or description" placeholderTextColor="#77758c" style={styles.input} />
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.chips}>
          {filters.map((filter) => <Pressable key={filter.value} onPress={() => setStatus(filter.value)} style={[styles.chip, status === filter.value && styles.activeChip]}><Text style={[styles.chipText, status === filter.value && styles.activeChipText]}>{filter.label}</Text></Pressable>)}
        </ScrollView>
        {query.isLoading ? <View style={styles.state}><ActivityIndicator color="#a78bfa" /></View> : null}
        {query.isError ? <View style={styles.state}><Text style={styles.stateText}>Unable to load tasks.</Text><Pressable onPress={() => void query.refetch()}><Text style={styles.back}>Try again</Text></Pressable></View> : null}
        {!query.isLoading && !query.isError && !tasks.length ? <View style={styles.state}><Text style={styles.stateText}>No matching tasks.</Text></View> : null}
        {tasks.map((task) => <Pressable key={task.id} onPress={() => router.push(`/task/view/${task.id}`)} style={styles.row}><View style={styles.copy}><Text style={styles.name}>{task.name}</Text><Text style={styles.meta}>{task.description}</Text></View><Text style={styles.status}>{task.status}</Text></Pressable>)}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  screen: { backgroundColor: '#0f1020', flex: 1 },
  content: { gap: 16, padding: 22, paddingTop: 58 },
  back: { color: '#c4b5fd', fontSize: 16, fontWeight: '700' },
  title: { color: '#f8f7ff', fontSize: 36, fontWeight: '800' },
  input: { backgroundColor: '#1b1b31', borderColor: '#302f4a', borderRadius: 12, borderWidth: 1, color: '#f8f7ff', padding: 14 },
  chips: { gap: 8 },
  chip: { backgroundColor: '#1b1b31', borderRadius: 18, paddingHorizontal: 14, paddingVertical: 9 },
  activeChip: { backgroundColor: '#8b5cf6' },
  chipText: { color: '#aaa7bd', fontWeight: '700' },
  activeChipText: { color: '#fff' },
  row: { alignItems: 'center', backgroundColor: '#1b1b31', borderRadius: 12, flexDirection: 'row', gap: 10, padding: 15 },
  copy: { flex: 1, gap: 5 },
  name: { color: '#f8f7ff', fontSize: 16, fontWeight: '700' },
  meta: { color: '#77758c' },
  status: { color: '#c4b5fd', fontSize: 11, fontWeight: '800' },
  state: { alignItems: 'center', gap: 12, paddingVertical: 80 },
  stateText: { color: '#aaa7bd' },
});
