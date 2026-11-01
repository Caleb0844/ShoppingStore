import { useLocalSearchParams, useRouter } from 'expo-router';
import { Pressable, ScrollView, StyleSheet, View } from 'react-native';
import { useQuery } from '@tanstack/react-query';
import { taskApi } from '@/api/taskApi';
import { TaskGroup, palette } from '@/components/TaskPresentation';
import { Text } from '@/components/Themed';

export default function DayScreen() { const router = useRouter(); const { date } = useLocalSearchParams<{ date: string }>(); const query = useQuery({ queryKey: ['tasks', 'day', date], queryFn: () => taskApi.week() }); const tasks = (query.data ?? []).filter((task) => task.dueDate.slice(0, 10) === date); const parsed = new Date(`${date}T12:00:00`); return <View style={styles.screen}><ScrollView contentContainerStyle={styles.content}><Pressable onPress={() => router.back()}><Text style={styles.back}>‹ Back</Text></Pressable><Text style={styles.title}>{parsed.toLocaleDateString('en-GB', { weekday: 'long' })}</Text><Text style={styles.subtitle}>{parsed.toLocaleDateString('en-GB', { day: 'numeric', month: 'long' })}</Text>{tasks.length ? <TaskGroup tasks={tasks} onPress={(task) => router.push(`/task/view/${task.id}`)} /> : <Text style={styles.empty}>Nothing planned for this day.</Text>}</ScrollView></View>; }
const styles = StyleSheet.create({ screen: { backgroundColor: palette.background, flex: 1 }, content: { gap: 14, padding: 18, paddingTop: 52 }, back: { color: palette.text, fontSize: 16 }, title: { color: palette.text, fontSize: 32, fontWeight: '500', marginTop: 18 }, subtitle: { color: palette.secondary }, empty: { color: palette.secondary, paddingVertical: 50, textAlign: 'center' } });
