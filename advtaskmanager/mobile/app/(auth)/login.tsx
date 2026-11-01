import { useRouter } from 'expo-router';
import { useState } from 'react';
import { Pressable, StyleSheet, TextInput, View } from 'react-native';
import { ApiError } from '../../api/errors';
import { useAuth } from '../../auth/AuthContext';
import { loginSchema } from '../../auth/schemas';
import { Text } from '../../components/Themed';

export default function LoginScreen() {
  const router = useRouter();
  const { signIn, loading } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  async function submit() {
    const result = loginSchema.safeParse({ username, password });
    if (!result.success) {
      setError(result.error.issues[0]?.message ?? 'Enter your username and password.');
      return;
    }
    try {
      setError('');
      await signIn(result.data);
    } catch (requestError) {
      setError(requestError instanceof ApiError && requestError.status === 429
        ? 'Too many sign-in attempts. Please wait before trying again.'
        : requestError instanceof Error ? requestError.message : 'Unable to sign in.');
    }
  }

  return (
    <View style={styles.container}>
      <View style={styles.card}>
        <Text style={styles.eyebrow}>ADV TASK MANAGER</Text>
        <Text style={styles.title}>Welcome back</Text>
        <Text style={styles.subtitle}>Sign in to stay on top of your work.</Text>
        <TextInput autoCapitalize="none" placeholder="Username" placeholderTextColor="#77758c" value={username} onChangeText={setUsername} style={styles.input} />
        <TextInput secureTextEntry placeholder="Password" placeholderTextColor="#77758c" value={password} onChangeText={setPassword} style={styles.input} />
        {error ? <Text style={styles.error}>{error}</Text> : null}
        <Pressable disabled={loading} onPress={submit} style={styles.button}>
          <Text style={styles.buttonText}>{loading ? 'Signing in…' : 'Sign in'}</Text>
        </Pressable>
        <Pressable onPress={() => router.push('/register')}>
          <Text style={styles.link}>Create an account</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', backgroundColor: '#0f1020', padding: 24 },
  card: { gap: 16 },
  eyebrow: { color: '#a78bfa', fontSize: 12, fontWeight: '700', letterSpacing: 2 },
  title: { color: '#f8f7ff', fontSize: 34, fontWeight: '800' },
  subtitle: { color: '#aaa7bd', fontSize: 16, marginBottom: 12 },
  input: { backgroundColor: '#1b1b31', borderColor: '#302f4a', borderRadius: 12, borderWidth: 1, color: '#f8f7ff', padding: 15 },
  error: { color: '#fb7185', lineHeight: 20 },
  button: { alignItems: 'center', backgroundColor: '#8b5cf6', borderRadius: 12, padding: 16 },
  buttonText: { color: '#fff', fontSize: 16, fontWeight: '700' },
  link: { color: '#c4b5fd', textAlign: 'center', padding: 8 },
});
