import { useLocalSearchParams, useRouter } from 'expo-router';
import { useEffect, useState } from 'react';
import { ActivityIndicator, Alert, Modal, Pressable, ScrollView, StyleSheet, View } from 'react-native';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { ApiError } from '../../../api/errors';
import { taskApi } from '../../../api/taskApi';
import { PostponeOption } from '../../../api/models';
import { useTaskOptions } from '../../../hooks/useTaskOptions';
import { Text } from '../../../components/Themed';

export default function TaskDetailsScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const router = useRouter();
  const queryClient = useQueryClient();
  const taskId = Number(id);
  const { data: task, isLoading, isError } = useQuery({
    queryKey: ['task', taskId],
    queryFn: () => taskApi.get(taskId),
    enabled: Number.isInteger(taskId),
  });
  const { data: options } = useTaskOptions();
  const [sheetOpen, setSheetOpen] = useState(false);
  const [conflict, setConflict] = useState<ApiError | null>(null);
  const [selectedOption, setSelectedOption] = useState<PostponeOption | null>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (isError) router.back();
  }, [isError, router]);

  async function remove() {
    Alert.alert('Delete task?', 'This cannot be undone.', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Delete', style: 'destructive', onPress: async () => {
        setSaving(true);
        try {
          await taskApi.remove(taskId);
          await queryClient.invalidateQueries({ queryKey: ['tasks'] });
          router.back();
        } catch (error) {
          Alert.alert('Unable to delete task', error instanceof Error ? error.message : 'Please try again.');
        } finally {
          setSaving(false);
        }
      } },
    ]);
  }

  async function postpone(option: PostponeOption, confirmOverlap = false) {
    setSaving(true);
    try {
      await taskApi.postpone(taskId, { option }, confirmOverlap);
      setConflict(null);
      setSelectedOption(null);
      setSheetOpen(false);
      await queryClient.invalidateQueries({ queryKey: ['tasks'] });
      await queryClient.invalidateQueries({ queryKey: ['task', taskId] });
    } catch (error) {
      if (error instanceof ApiError && error.status === 409 && error.confirmationRequired) {
        setSelectedOption(option);
        setConflict(error);
      } else {
        Alert.alert('Unable to postpone task', error instanceof Error ? error.message : 'Please try again.');
      }
    } finally {
      setSaving(false);
    }
  }

  if (isLoading || !task) return <View style={styles.center}><ActivityIndicator color="#a78bfa" /></View>;

  return (
    <View style={styles.screen}>
      <ScrollView contentContainerStyle={styles.content}>
        <Pressable onPress={() => router.back()}><Text style={styles.back}>‹ Back</Text></Pressable>
        <Text style={styles.title}>{task.name}</Text>
        <View style={styles.badge}><Text style={styles.badgeText}>{task.status}</Text></View>
        <Text style={styles.description}>{task.description}</Text>
        <Detail label="Priority" value={task.priority} />
        <Detail label="Due" value={formatDate(task.dueDate)} />
        {task.startsAt ? <Detail label="Starts" value={formatDate(task.startsAt)} /> : null}
        {task.endsAt ? <Detail label="Ends" value={formatDate(task.endsAt)} /> : null}
        <Detail label="Repeat" value={task.repetition} />
        <Detail label="Reminder" value={task.reminder === 'CUSTOM' ? `${task.reminderAmount ?? ''} ${task.reminderUnit ?? ''}` : task.reminder} />
        <View style={styles.actions}>
          <Pressable onPress={() => router.push(`/task/${task.id}`)} style={styles.primary}><Text style={styles.primaryText}>Edit</Text></Pressable>
          <Pressable disabled={saving} onPress={() => setSheetOpen(true)} style={styles.secondary}><Text style={styles.secondaryText}>Postpone</Text></Pressable>
          <Pressable disabled={saving} onPress={() => void remove()} style={styles.danger}><Text style={styles.dangerText}>Delete</Text></Pressable>
        </View>
      </ScrollView>
      <Modal visible={sheetOpen} transparent animationType="slide" onRequestClose={() => setSheetOpen(false)}>
        <Pressable style={styles.sheetBackdrop} onPress={() => setSheetOpen(false)}>
          <View style={styles.sheet} onStartShouldSetResponder={() => true}>
            <Text style={styles.sheetTitle}>Postpone task</Text>
            <Text style={styles.sheetMessage}>Choose when to reschedule it.</Text>
            {(options?.postponeOptions ?? []).map((choice) => (
              <Pressable key={choice.value} disabled={saving} onPress={() => void postpone(choice.value as PostponeOption)} style={styles.option}>
                <Text style={styles.optionText}>{choice.label}</Text>
              </Pressable>
            ))}
            {conflict ? (
              <View style={styles.conflict}>
                <Text style={styles.conflictText}>{conflict.message}</Text>
                <View style={styles.conflictActions}>
                  <Pressable onPress={() => setConflict(null)}><Text style={styles.secondaryText}>Cancel</Text></Pressable>
                  <Pressable disabled={!selectedOption} onPress={() => selectedOption && void postpone(selectedOption, true)}><Text style={styles.primaryText}>Confirm anyway</Text></Pressable>
                </View>
              </View>
            ) : null}
            <Pressable onPress={() => setSheetOpen(false)}><Text style={styles.cancel}>Close</Text></Pressable>
          </View>
        </Pressable>
      </Modal>
    </View>
  );
}

function Detail({ label, value }: { label: string; value: string }) {
  return <View style={styles.detail}><Text style={styles.detailLabel}>{label}</Text><Text style={styles.detailValue}>{value}</Text></View>;
}

function formatDate(value: string) {
  return new Date(value).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
}

const styles = StyleSheet.create({
  screen: { backgroundColor: '#0f1020', flex: 1 },
  content: { gap: 18, padding: 22, paddingTop: 58 },
  center: { alignItems: 'center', backgroundColor: '#0f1020', flex: 1, justifyContent: 'center' },
  back: { color: '#c4b5fd', fontSize: 16, fontWeight: '700' },
  title: { color: '#f8f7ff', fontSize: 36, fontWeight: '800' },
  badge: { alignSelf: 'flex-start', backgroundColor: '#30265e', borderRadius: 16, paddingHorizontal: 10, paddingVertical: 6 },
  badgeText: { color: '#c4b5fd', fontSize: 12, fontWeight: '800' },
  description: { color: '#aaa7bd', fontSize: 16, lineHeight: 24 },
  detail: { borderBottomColor: '#302f4a', borderBottomWidth: 1, gap: 5, paddingBottom: 14 },
  detailLabel: { color: '#77758c', fontSize: 12, fontWeight: '700', textTransform: 'uppercase' },
  detailValue: { color: '#f8f7ff', fontSize: 16 },
  actions: { gap: 10, marginTop: 12 },
  primary: { alignItems: 'center', backgroundColor: '#8b5cf6', borderRadius: 12, padding: 15 },
  primaryText: { color: '#fff', fontWeight: '800' },
  secondary: { alignItems: 'center', backgroundColor: '#272641', borderRadius: 12, padding: 15 },
  secondaryText: { color: '#c4b5fd', fontWeight: '800' },
  danger: { alignItems: 'center', borderColor: '#7f304b', borderRadius: 12, borderWidth: 1, padding: 15 },
  dangerText: { color: '#fb7185', fontWeight: '800' },
  sheetBackdrop: { backgroundColor: 'rgba(0,0,0,0.65)', flex: 1, justifyContent: 'flex-end' },
  sheet: { backgroundColor: '#1b1b31', borderTopLeftRadius: 22, borderTopRightRadius: 22, gap: 12, padding: 24, paddingBottom: 36 },
  sheetTitle: { color: '#f8f7ff', fontSize: 22, fontWeight: '800' },
  sheetMessage: { color: '#aaa7bd', marginBottom: 6 },
  option: { backgroundColor: '#272641', borderRadius: 10, padding: 15 },
  optionText: { color: '#f8f7ff', fontSize: 16, fontWeight: '700' },
  conflict: { backgroundColor: '#41243d', borderRadius: 10, gap: 12, padding: 14 },
  conflictText: { color: '#ffd7e1' },
  conflictActions: { alignItems: 'center', flexDirection: 'row', justifyContent: 'flex-end', gap: 20 },
  cancel: { color: '#aaa7bd', fontWeight: '700', paddingVertical: 8 },
});
