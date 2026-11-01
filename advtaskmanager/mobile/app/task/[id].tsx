import DateTimePicker, { DateTimePickerEvent } from '@react-native-community/datetimepicker';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { useEffect, useMemo, useState } from 'react';
import { ActivityIndicator, Modal, Pressable, ScrollView, StyleSheet, TextInput, View } from 'react-native';
import { ApiError } from '../../api/errors';
import { taskApi } from '../../api/taskApi';
import { Priority, Repetition, Reminder, ReminderUnit, Task, TaskInput, TaskOptions, TaskUpdate } from '../../api/models';
import { useTaskOptions } from '../../hooks/useTaskOptions';
import { getDefaultReminder } from '../../settings/preferences';
import { TaskForm, taskFormSchema } from '../../tasks/schemas';
import { formatDateLabel, formatLocalDateTime, formatTimeLabel } from '../../utils/dates';
import { Text } from '../../components/Themed';

const initialForm: TaskForm = {
  name: '', description: '', priority: undefined, dueDate: '', startsAt: '', endsAt: '',
  repetition: 'NONE', reminder: 'NONE', reminderAmount: '', reminderUnit: undefined,
};

const readable = (value: string) => value.replaceAll('_', ' ').toLowerCase().replace(/^\w/, (char) => char.toUpperCase());
const wallDate = (value?: string) => value ? new Date(`${value}:00`) : new Date();
const toForm = (task: Task): TaskForm => ({
  name: task.name, description: task.description, priority: task.priority,
  dueDate: task.dueDate?.slice(0, 16) ?? '', startsAt: task.startsAt?.slice(0, 16) ?? '',
  endsAt: task.endsAt?.slice(0, 16) ?? '', repetition: task.repetition, reminder: task.reminder,
  reminderAmount: task.reminderAmount?.toString() ?? '', reminderUnit: task.reminderUnit,
});

function ChoiceSheet<T extends string>({ title, values, selected, labels, onSelect, onClose }: {
  title: string; values: T[]; selected?: T; labels?: Record<string, string>;
  onSelect: (value: T) => void; onClose: () => void;
}) {
  return <Modal transparent animationType="slide" visible onRequestClose={onClose}>
    <Pressable style={styles.modalBackdrop} onPress={onClose}><View style={styles.sheet}>
      <Text style={styles.sheetTitle}>{title}</Text>
      {values.map((value) => <Pressable key={value} style={styles.sheetRow} onPress={() => { onSelect(value); onClose(); }}>
        <Text style={styles.sheetText}>{labels?.[value] ?? readable(value)}</Text>
        <Text style={styles.radio}>{selected === value ? '●' : '○'}</Text>
      </Pressable>)}
    </View></Pressable>
  </Modal>;
}

export default function TaskEditorScreen() {
  const router = useRouter();
  const { id, selectedDate } = useLocalSearchParams<{ id?: string; selectedDate?: string }>();
  const editing = Boolean(id && id !== 'new');
  const { data: options, isLoading: optionsLoading } = useTaskOptions();
  const [form, setForm] = useState<TaskForm>(initialForm);
  const [original, setOriginal] = useState<TaskForm | null>(null);
  const [loading, setLoading] = useState(editing);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [conflict, setConflict] = useState<ApiError | null>(null);
  const [picker, setPicker] = useState<'date' | 'start' | 'end' | null>(null);
  const [sheet, setSheet] = useState<'repetition' | 'reminder' | 'unit' | null>(null);

  const optionValues: TaskOptions = options ?? { statuses: [], views: [], repetitions: [], priorities: [], reminders: [], reminderUnits: [], postponeOptions: [] };
  useEffect(() => {
    if (!id || !editing) return;
    void taskApi.get(Number(id)).then((task) => { const value = toForm(task); setForm(value); setOriginal(value); })
      .catch((requestError) => setError(requestError instanceof Error ? requestError.message : 'Unable to load task.'))
      .finally(() => setLoading(false));
  }, [id, editing]);
  useEffect(() => {
    if (!editing) {
      const date = selectedDate ? `${selectedDate}T09:00` : formatLocalDateTime(new Date()).slice(0, 16);
      void getDefaultReminder().then((reminder) => setForm((current) => ({ ...current, dueDate: date, reminder })));
    }
  }, [editing, selectedDate]);

  const update = <K extends keyof TaskForm>(key: K, value: TaskForm[K]) => setForm((current) => ({ ...current, [key]: value }));
  const parsed = taskFormSchema.safeParse(form);
  const fieldErrors = useMemo(() => {
    const result: Record<string, string> = {};
    if (!parsed.success) parsed.error.issues.forEach((issue) => { const key = String(issue.path[0] ?? 'form'); if (!result[key]) result[key] = issue.message; });
    return result;
  }, [form, parsed.success]);

  function onPickerChange(event: DateTimePickerEvent, value?: Date) {
    setPicker(null);
    if (event.type === 'dismissed' || !value) return;
    const key = picker;
    if (key === 'date') update('dueDate', formatLocalDateTime(value).slice(0, 16));
    if (key === 'start') update('startsAt', formatLocalDateTime(value).slice(0, 16));
    if (key === 'end') update('endsAt', formatLocalDateTime(value).slice(0, 16));
  }

  const notSetPriority = optionValues.priorities.find((value) => value.toLowerCase().includes('not_set') || value.toLowerCase().includes('not set'));
  const payload = (value: TaskForm): TaskInput => ({
    name: value.name, description: value.description, priority: (value.priority ?? notSetPriority) as Priority,
    dueDate: value.dueDate, startsAt: value.startsAt, endsAt: value.endsAt || undefined,
    repetition: value.repetition as Repetition, reminder: value.reminder as Reminder,
    reminderAmount: value.reminder === 'CUSTOM' ? Number(value.reminderAmount) : undefined,
    reminderUnit: value.reminder === 'CUSTOM' ? value.reminderUnit as ReminderUnit : undefined,
  });
  const changedPayload = (value: TaskForm): TaskUpdate => {
    const next = payload(value);
    if (!original) return next;
    return Object.fromEntries(Object.entries(next).filter(([key, item]) => item !== (original as Record<string, unknown>)[key])) as TaskUpdate;
  };
  async function save(confirmOverlap = false) {
    if (!parsed.success) { setError('Please fix the highlighted fields.'); return; }
    setSaving(true); setError('');
    try {
      if (editing) await taskApi.update(Number(id), changedPayload(parsed.data), confirmOverlap);
      else await taskApi.create(payload(parsed.data), confirmOverlap);
      router.back();
    } catch (requestError) {
      if (requestError instanceof ApiError && requestError.status === 409 && requestError.confirmationRequired) setConflict(requestError);
      else setError(requestError instanceof Error ? requestError.message : 'Unable to save task.');
    } finally { setSaving(false); }
  }

  if (loading || optionsLoading) return <View style={styles.center}><ActivityIndicator color="#f2f2f2" /></View>;
  const due = wallDate(form.dueDate), start = wallDate(form.startsAt), end = wallDate(form.endsAt);
  const reminderLabels = Object.fromEntries(optionValues.reminders.map((choice) => [choice.value, choice.label]));
  return <ScrollView style={styles.screen} contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
    <Pressable onPress={() => router.back()}><Text style={styles.back}>‹ Back</Text></Pressable>
    <Text style={styles.title}>{editing ? 'Edit task' : 'New task'}</Text>
    <Field label="Name" value={form.name} error={fieldErrors.name} onChangeText={(value) => update('name', value)} />
    <Field label="Description" value={form.description} error={fieldErrors.description} onChangeText={(value) => update('description', value)} multiline />
    <View style={styles.field}><Text style={styles.label}>Priority</Text><View style={styles.choices}>{optionValues.priorities.map((value) =>
      <Pressable key={value} onPress={() => update('priority', value)} style={[styles.choice, form.priority === value && styles.selectedChoice]}><Text style={styles.choiceText}>{readable(value)}</Text></Pressable>)}</View><Text style={styles.hint}>Optional; no selection uses the backend not-set value.</Text></View>
    <DateField label="Due date" value={form.dueDate ? formatDateLabel(due) : 'Choose a date'} error={fieldErrors.dueDate} onPress={() => setPicker('date')} />
    <DateField label="Start time" value={form.startsAt ? formatTimeLabel(start) : 'Choose a time'} error={fieldErrors.startsAt} onPress={() => setPicker('start')} />
    <DateField label="End time" value={form.endsAt ? formatTimeLabel(end) : 'Optional'} error={fieldErrors.endsAt} onPress={() => setPicker('end')} clear={Boolean(form.endsAt)} onClear={() => update('endsAt', '')} />
    <SheetField label="Repeat" value={readable(form.repetition)} onPress={() => setSheet('repetition')} />
    <SheetField label="Reminder" value={reminderLabels[form.reminder] ?? readable(form.reminder)} onPress={() => setSheet('reminder')} />
    {form.reminder === 'CUSTOM' ? <><Field label="Custom amount" value={form.reminderAmount ?? ''} error={fieldErrors.reminderAmount} keyboardType="numeric" onChangeText={(value) => update('reminderAmount', value)} /><SheetField label="Custom unit" value={form.reminderUnit ? readable(form.reminderUnit) : 'Choose a unit'} error={fieldErrors.reminderUnit} onPress={() => setSheet('unit')} /></> : null}
    {error ? <Text style={styles.error}>{error}</Text> : null}
    <Pressable disabled={saving || !parsed.success || !notSetPriority} onPress={() => void save()} style={[styles.save, (!parsed.success || saving || !notSetPriority) && styles.disabled]}><Text style={styles.saveText}>{saving ? 'Saving…' : editing ? 'Save changes' : 'Create task'}</Text></Pressable>
    {picker ? <DateTimePicker value={picker === 'date' ? due : picker === 'start' ? start : end} mode={picker === 'date' ? 'date' : 'time'} onChange={onPickerChange} /> : null}
    {sheet === 'repetition' ? <ChoiceSheet title="Repeat" values={optionValues.repetitions} selected={form.repetition} onSelect={(value) => update('repetition', value)} onClose={() => setSheet(null)} /> : null}
    {sheet === 'reminder' ? <ChoiceSheet title="Reminder" values={optionValues.reminders.map((choice) => choice.value)} labels={reminderLabels} selected={form.reminder} onSelect={(value) => update('reminder', value as Reminder)} onClose={() => setSheet(null)} /> : null}
    {sheet === 'unit' ? <ChoiceSheet title="Reminder unit" values={optionValues.reminderUnits} selected={form.reminderUnit} onSelect={(value) => update('reminderUnit', value)} onClose={() => setSheet(null)} /> : null}
    <OverlapDialog error={conflict} onCancel={() => setConflict(null)} onConfirm={() => { setConflict(null); void save(true); }} />
  </ScrollView>;
}

function Field({ label, value, error, onChangeText, multiline, keyboardType }: { label: string; value: string; error?: string; onChangeText: (value: string) => void; multiline?: boolean; keyboardType?: 'default' | 'numeric' }) {
  return <View style={styles.field}><Text style={styles.label}>{label}</Text><TextInput value={value} onChangeText={onChangeText} multiline={multiline} placeholderTextColor="#9a9a9f" keyboardType={keyboardType} style={[styles.input, multiline && styles.multiline]} />{error ? <Text style={styles.error}>{error}</Text> : null}</View>;
}
function DateField({ label, value, error, onPress, clear, onClear }: { label: string; value: string; error?: string; onPress: () => void; clear?: boolean; onClear?: () => void }) {
  return <View style={styles.field}><Text style={styles.label}>{label}</Text><View style={styles.dateField}><Pressable onPress={onPress} style={styles.dateButton}><Text style={styles.dateText}>{value}</Text><Text style={styles.icon}>{label.includes('time') ? '◷' : '▣'}</Text></Pressable>{clear ? <Pressable onPress={onClear} style={styles.clear}><Text style={styles.dateText}>×</Text></Pressable> : null}</View>{error ? <Text style={styles.error}>{error}</Text> : null}</View>;
}
function SheetField({ label, value, error, onPress }: { label: string; value: string; error?: string; onPress: () => void }) {
  return <View style={styles.field}><Text style={styles.label}>{label}</Text><Pressable onPress={onPress} style={styles.dateField}><Text style={styles.dateText}>{value}</Text><Text style={styles.icon}>›</Text></Pressable>{error ? <Text style={styles.error}>{error}</Text> : null}</View>;
}
function OverlapDialog({ error, onCancel, onConfirm }: { error: ApiError | null; onCancel: () => void; onConfirm: () => void }) {
  if (!error) return null;
  const task = error.conflictingTasks?.[0];
  return <Modal transparent visible onRequestClose={onCancel}><View style={styles.modalBackdrop}><View style={styles.dialog}><Text style={styles.sheetTitle}>Task overlap</Text><Text style={styles.dialogText}>{error.message}</Text>{task ? <Text style={styles.dialogText}>{task.name}</Text> : null}<View style={styles.actions}><Pressable onPress={onCancel}><Text style={styles.cancel}>Cancel</Text></Pressable><Pressable onPress={onConfirm} style={styles.confirm}><Text style={styles.saveText}>Confirm anyway</Text></Pressable></View></View></View></Modal>;
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: '#000' }, content: { gap: 16, padding: 22, paddingTop: 58, paddingBottom: 48 }, center: { flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: '#000' },
  back: { color: '#f2f2f2', fontSize: 16 }, title: { color: '#f2f2f2', fontSize: 32, fontWeight: '500' }, field: { gap: 7 }, label: { color: '#9a9a9f', fontSize: 14 }, input: { backgroundColor: '#121214', borderColor: '#2a2a2c', borderWidth: 1, borderRadius: 14, color: '#f2f2f2', padding: 14 }, multiline: { minHeight: 90, textAlignVertical: 'top' },
  choices: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 }, choice: { backgroundColor: '#1c1c1e', borderRadius: 20, paddingHorizontal: 14, paddingVertical: 10 }, selectedChoice: { backgroundColor: '#3a3a3c' }, choiceText: { color: '#f2f2f2' }, hint: { color: '#9a9a9f', fontSize: 12 }, dateField: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#121214', borderColor: '#2a2a2c', borderWidth: 1, borderRadius: 14, minHeight: 52 }, dateButton: { flex: 1, flexDirection: 'row', justifyContent: 'space-between', padding: 14 }, dateText: { color: '#f2f2f2' }, icon: { color: '#9a9a9f', fontSize: 20 }, clear: { padding: 14 }, error: { color: '#e24b4a', fontSize: 12 }, save: { alignItems: 'center', backgroundColor: '#f2f2f2', borderRadius: 24, padding: 16 }, disabled: { opacity: 0.45 }, saveText: { color: '#000', fontWeight: '500' },
  modalBackdrop: { flex: 1, justifyContent: 'flex-end', backgroundColor: 'rgba(0,0,0,0.65)' }, sheet: { backgroundColor: '#121214', borderTopLeftRadius: 14, borderTopRightRadius: 14, padding: 22, gap: 4 }, sheetTitle: { color: '#f2f2f2', fontSize: 20, fontWeight: '500', marginBottom: 8 }, sheetRow: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 15 }, sheetText: { color: '#f2f2f2', fontSize: 16 }, radio: { color: '#f2f2f2', fontSize: 20 }, dialog: { backgroundColor: '#121214', borderRadius: 14, margin: 22, padding: 20, gap: 14 }, dialogText: { color: '#f2f2f2' }, actions: { flexDirection: 'row', justifyContent: 'flex-end', alignItems: 'center', gap: 18 }, cancel: { color: '#9a9a9f', padding: 12 }, confirm: { backgroundColor: '#f2f2f2', borderRadius: 20, padding: 12 },
});
