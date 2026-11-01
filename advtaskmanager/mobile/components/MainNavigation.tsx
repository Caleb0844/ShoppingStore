import { SymbolView } from 'expo-symbols';
import { useRouter } from 'expo-router';
import { Modal, Pressable, StyleSheet, View } from 'react-native';
import { useQuery } from '@tanstack/react-query';
import { taskApi } from '../api/taskApi';
import { useAuth } from '../auth/AuthContext';
import { Text } from './Themed';
import { palette } from './TaskPresentation';
import { t } from '../i18n';

export function MainTopBar({ active, onDrawer, onLists, overdue = false }: { active: 'today' | 'week' | 'month'; onDrawer: () => void; onLists: () => void; overdue?: boolean }) {
  const router = useRouter();
  const overdueQuery = useQuery({ queryKey: ['tasks', 'overdue'], queryFn: taskApi.overdue });
  const hasOverdue = overdue || Boolean(overdueQuery.data?.length);
  return <View style={styles.top}><Pressable onPress={onDrawer} style={styles.round}><SymbolView name={{ ios: 'sidebar.left', android: 'menu', web: 'menu' }} tintColor={palette.text} size={24} /></Pressable><View style={styles.switcher}>{(['today', 'week', 'month'] as const).map((key) => <Pressable key={key} onPress={() => router.push(key === 'today' ? '/(tabs)' : `/(tabs)/${key}`)} style={[styles.switchItem, active === key && styles.selected]}><Text style={[styles.switchText, active === key && styles.selectedText]}>{t(key)}</Text></Pressable>)}</View><Pressable onPress={onLists} style={styles.round}><SymbolView name={{ ios: 'list.bullet', android: 'view_agenda', web: 'view_agenda' }} tintColor={palette.text} size={24} />{hasOverdue ? <View style={styles.dot} /> : null}</Pressable></View>;
}

export function Drawer({ visible, onClose }: { visible: boolean; onClose: () => void }) {
  const router = useRouter(); const { user, signOut } = useAuth();
  return <Modal visible={visible} transparent animationType="slide" onRequestClose={onClose}><View style={styles.overlay}><Pressable style={styles.backdrop} onPress={onClose} /><View style={styles.drawer}><Text style={styles.drawerTitle}>{user?.name ?? t('profile')}</Text><Text style={styles.drawerSub}>{user?.email}</Text><Pressable onPress={() => { onClose(); router.push('/profile'); }} style={styles.item}><Text style={styles.itemText}>{t('profile')}</Text></Pressable><Pressable onPress={() => { onClose(); router.push('/settings'); }} style={styles.item}><Text style={styles.itemText}>{t('settings')}</Text></Pressable><Pressable onPress={() => void signOut()} style={styles.item}><Text style={styles.signOut}>{t('signOut')}</Text></Pressable></View></View></Modal>;
}

export function ListsSheet({ visible, onClose }: { visible: boolean; onClose: () => void }) {
  const router = useRouter(); const counts = useQuery({ queryKey: ['tasks', 'lists'], queryFn: () => taskApi.list() });
  const tasks = counts.data ?? [];
  const rows = [{ key: 'overdue', label: t('overdue'), count: tasks.filter((x) => new Date(x.dueDate) < new Date() && !x.completed).length }, { key: 'postponed', label: t('postponed'), count: tasks.filter((x) => x.status === 'POSTPONED').length }, { key: 'cancelled', label: t('cancelled'), count: 0 }, { key: 'completed', label: t('completed'), count: tasks.filter((x) => x.completed).length }];
  return <Modal visible={visible} transparent animationType="slide" onRequestClose={onClose}><Pressable style={styles.sheetOverlay} onPress={onClose}><View style={styles.sheet} onStartShouldSetResponder={() => true}><Text style={styles.sheetTitle}>{t('lists')}</Text>{rows.map((row) => <Pressable key={row.key} onPress={() => { onClose(); router.push({ pathname: '/(tabs)/all', params: { status: row.key === 'completed' ? 'COMPLETED' : 'ALL', overdue: row.key === 'overdue' ? 'true' : undefined } }); }} style={styles.listRow}><Text style={styles.itemText}>{row.label}</Text><Text style={styles.count}>{row.count}</Text></Pressable>)}</View></Pressable></Modal>;
}

const styles = StyleSheet.create({ top: { alignItems: 'center', flexDirection: 'row', justifyContent: 'space-between', paddingTop: 12 }, round: { alignItems: 'center', backgroundColor: palette.button, borderRadius: 22, height: 44, justifyContent: 'center', position: 'relative', width: 44 }, dot: { backgroundColor: palette.red, borderRadius: 4, height: 7, position: 'absolute', right: 3, top: 3, width: 7 }, switcher: { backgroundColor: palette.button, borderRadius: 24, flexDirection: 'row', padding: 3 }, switchItem: { borderRadius: 20, paddingHorizontal: 11, paddingVertical: 9 }, selected: { backgroundColor: '#3a3a3c' }, switchText: { color: palette.secondary, fontSize: 15 }, selectedText: { color: palette.text, fontWeight: '500' }, overlay: { backgroundColor: 'rgba(0,0,0,.7)', flex: 1, flexDirection: 'row' }, backdrop: { flex: 1 }, drawer: { backgroundColor: palette.group, borderRightColor: palette.border, borderRightWidth: 1, padding: 24, paddingTop: 64, width: 285 }, drawerTitle: { color: palette.text, fontSize: 22, fontWeight: '500' }, drawerSub: { color: palette.secondary, marginTop: 5, marginBottom: 24 }, item: { borderBottomColor: palette.border, borderBottomWidth: 1, paddingVertical: 17 }, itemText: { color: palette.text, fontSize: 16, fontWeight: '500' }, signOut: { color: palette.red, fontSize: 16, fontWeight: '500' }, sheetOverlay: { backgroundColor: 'rgba(0,0,0,.7)', flex: 1, justifyContent: 'flex-end' }, sheet: { backgroundColor: palette.group, borderTopLeftRadius: 14, borderTopRightRadius: 14, gap: 3, padding: 20, paddingBottom: 32 }, sheetTitle: { color: palette.text, fontSize: 22, fontWeight: '500', marginBottom: 8 }, listRow: { alignItems: 'center', borderBottomColor: palette.border, borderBottomWidth: 1, flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 16 }, count: { color: palette.secondary } });
