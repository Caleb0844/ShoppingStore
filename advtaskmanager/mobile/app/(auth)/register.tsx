import { useRouter } from 'expo-router';
import { useState } from 'react';
import { Pressable, ScrollView, StyleSheet, TextInput, View } from 'react-native';
import { ApiError } from '../../api/errors';
import { userApi } from '../../api/userApi';
import { registerSchema } from '../../auth/schemas';
import { Text } from '../../components/Themed';

export default function RegisterScreen() {
  const router = useRouter();
  const [form, setForm] = useState({ name: '', username: '', email: '', password: '', confirmPassword: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const update = (key: keyof typeof form) => (value: string) => setForm((current) => ({ ...current, [key]: value }));

  async function submit() {
    const result = registerSchema.safeParse(form);
    if (!result.success) {
      setError(result.error.issues[0]?.message ?? 'Check the form and try again.');
      return;
    }
    setLoading(true);
    try {
      setError('');
      await userApi.create({ name: result.data.name, username: result.data.username, email: result.data.email, password: result.data.password });
      router.replace('/login');
    } catch (requestError) {
      if (requestError instanceof ApiError && requestError.status === 409) {
        setError('That username is already taken. Choose a different username.');
      } else if (requestError instanceof ApiError && requestError.status === 429) {
        setError('Too many registration attempts. Please wait before trying again.');
      } else {
        setError(requestError instanceof Error ? requestError.message : 'Unable to create your account.');
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <ScrollView contentContainerStyle={styles.container} keyboardShouldPersistTaps="handled">
      <View style={styles.card}>
        <Text style={styles.eyebrow}>GET STARTED</Text>
        <Text style={styles.title}>Create account</Text>
        <Text style={styles.subtitle}>Organize your day with less friction.</Text>
        {(['name', 'username', 'email'] as const).map((field) => (
          <TextInput key={field} autoCapitalize={field === 'email' ? 'none' : 'words'} keyboardType={field === 'email' ? 'email-address' : 'default'} placeholder={field[0].toUpperCase() + field.slice(1)} placeholderTextColor="#77758c" value={form[field]} onChangeText={update(field)} style={styles.input} />
        ))}
        <TextInput secureTextEntry placeholder="Password (8+ characters)" placeholderTextColor="#77758c" value={form.password} onChangeText={update('password')} style={styles.input} />
        <TextInput secureTextEntry placeholder="Confirm password" placeholderTextColor="#77758c" value={form.confirmPassword} onChangeText={update('confirmPassword')} style={styles.input} />
        {error ? <Text style={styles.error}>{error}</Text> : null}
        <Pressable disabled={loading} onPress={submit} style={styles.button}>
          <Text style={styles.buttonText}>{loading ? 'Creating account…' : 'Create account'}</Text>
        </Pressable>
        <Pressable onPress={() => router.replace('/login')}>
          <Text style={styles.link}>Already have an account? Sign in</Text>
        </Pressable>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flexGrow: 1, justifyContent: 'center', backgroundColor: '#0f1020', padding: 24 },
  card: { gap: 14 },
  eyebrow: { color: '#a78bfa', fontSize: 12, fontWeight: '700', letterSpacing: 2 },
  title: { color: '#f8f7ff', fontSize: 34, fontWeight: '800' },
  subtitle: { color: '#aaa7bd', fontSize: 16, marginBottom: 8 },
  input: { backgroundColor: '#1b1b31', borderColor: '#302f4a', borderRadius: 12, borderWidth: 1, color: '#f8f7ff', padding: 15 },
  error: { color: '#fb7185', lineHeight: 20 },
  button: { alignItems: 'center', backgroundColor: '#8b5cf6', borderRadius: 12, padding: 16 },
  buttonText: { color: '#fff', fontSize: 16, fontWeight: '700' },
  link: { color: '#c4b5fd', textAlign: 'center', padding: 8 },
});
