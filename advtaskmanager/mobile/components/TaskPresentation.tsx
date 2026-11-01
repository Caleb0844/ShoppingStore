import { SymbolView } from 'expo-symbols';
import { Pressable, StyleSheet, TextInput, View } from 'react-native';
import { Task } from '../api/models';
import { Text } from './Themed';
import { t } from '../i18n';

export const palette = {
  background: '#000000', group: '#121214', button: '#1c1c1e', chip: '#2c2c2e',
  border: '#2a2a2c', text: '#f2f2f2', secondary: '#9a9a9f', red: '#e24b4a', amber: '#ef9f27',
};

export function priorityColor(priority: Task['priority']) {
  return priority === 'HIGH' ? palette.red : priority === 'MEDIUM' ? palette.amber : '#77777b';
}

export function TaskRow({ task, onToggle, onPress }: { task: Task; onToggle?: () => void; onPress?: () => void }) {
  const content = (
    <View style={styles.row}>
      <View style={styles.copy}>
        <Text style={[styles.name, task.completed && styles.struck]}>{task.name}</Text>
        <View style={styles.meta}><View style={[styles.priority, { backgroundColor: priorityColor(task.priority) }]} /><Text style={styles.description}>{task.description}</Text></View>
      </View>
      <Text style={styles.time}>{task.dueDate ? new Date(task.dueDate).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' }) : ''}</Text>
      {onToggle ? <Pressable onPress={onToggle} accessibilityRole="checkbox" accessibilityState={{ checked: task.completed }} style={[styles.circle, { borderColor: priorityColor(task.priority) }, task.completed && styles.done]}>{task.completed ? <Text style={styles.check}>✓</Text> : null}</Pressable> : null}
    </View>
  );
  return onPress ? <Pressable onPress={onPress}>{content}</Pressable> : content;
}

export function TaskGroup({ title, tasks, onToggle, onPress }: { title?: string; tasks: Task[]; onToggle?: (task: Task) => void; onPress?: (task: Task) => void }) {
  if (!tasks.length) return null;
  return <View style={styles.group}>{title ? <Text style={styles.groupTitle}>{title}</Text> : null}{tasks.map((task, index) => <View key={task.id} style={[index > 0 && styles.separator]}><TaskRow task={task} onToggle={onToggle ? () => onToggle(task) : undefined} onPress={onPress ? () => onPress(task) : undefined} /></View>)}</View>;
}

export function BottomBar({ query, onQueryChange, onCreate }: { query: string; onQueryChange: (value: string) => void; onCreate: () => void }) {
  return <View style={styles.bottom}><View style={styles.search}><SymbolView name={{ ios: 'magnifyingglass', android: 'search', web: 'search' }} tintColor={palette.secondary} size={18} /><TextInput value={query} onChangeText={onQueryChange} placeholder={t('search')} placeholderTextColor={palette.secondary} style={styles.searchInput} /></View><Pressable onPress={onCreate} style={styles.pencil}><SymbolView name={{ ios: 'pencil', android: 'edit', web: 'edit' }} tintColor={palette.text} size={19} /></Pressable></View>;
}

const styles = StyleSheet.create({
  group: { backgroundColor: palette.group, borderColor: palette.border, borderRadius: 14, borderWidth: 1, overflow: 'hidden' },
  groupTitle: { color: palette.secondary, fontSize: 13, paddingBottom: 8, paddingHorizontal: 14, paddingTop: 14 },
  row: { alignItems: 'center', flexDirection: 'row', minHeight: 72, paddingHorizontal: 14, paddingVertical: 11 },
  separator: { borderTopColor: palette.border, borderTopWidth: 1 },
  copy: { flex: 1, gap: 6 },
  name: { color: palette.text, fontSize: 16, fontWeight: '500' },
  struck: { color: palette.secondary, textDecorationLine: 'line-through' },
  meta: { alignItems: 'center', flexDirection: 'row', gap: 6 },
  priority: { borderRadius: 3, height: 6, width: 6 },
  description: { color: palette.secondary, flex: 1, fontSize: 12 },
  time: { color: palette.secondary, fontSize: 12, marginHorizontal: 10 },
  circle: { alignItems: 'center', borderRadius: 14, borderWidth: 2, height: 28, justifyContent: 'center', width: 28 },
  done: { backgroundColor: palette.text, borderColor: palette.text },
  check: { color: '#000', fontWeight: '500' },
  bottom: { alignItems: 'center', backgroundColor: palette.background, bottom: 0, flexDirection: 'row', gap: 10, left: 0, padding: 14, paddingBottom: 26, position: 'absolute', right: 0 },
  search: { alignItems: 'center', backgroundColor: palette.button, borderColor: palette.border, borderRadius: 24, borderWidth: 1, flex: 1, flexDirection: 'row', paddingHorizontal: 14 },
  searchInput: { color: palette.text, flex: 1, padding: 12 },
  pencil: { alignItems: 'center', backgroundColor: palette.button, borderRadius: 24, height: 48, justifyContent: 'center', width: 48 },
});
