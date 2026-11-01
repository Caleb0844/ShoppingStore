import { Redirect, Slot, useSegments } from 'expo-router';
import { ActivityIndicator, StyleSheet, View } from 'react-native';
import { useAuth } from './AuthContext';

export function AuthGuard() {
  const { user, loading } = useAuth();
  const segments = useSegments();
  const inAuthGroup = segments[0] === '(auth)';
  if (loading) {
    return <View style={styles.loading}><ActivityIndicator color="#8b5cf6" /></View>;
  }
  if (!user && !inAuthGroup) return <Redirect href="/login" />;
  if (user && inAuthGroup) return <Redirect href="/(tabs)" />;
  return <Slot />;
}

const styles = StyleSheet.create({
  loading: { flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: '#0f1020' },
});
