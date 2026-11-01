import { Tabs } from 'expo-router';

export default function TabLayout() {
  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarStyle: { display: 'none' },
      }}>
      <Tabs.Screen
        name="index"
        options={{ title: 'Today' }}
      />
      <Tabs.Screen name="week" options={{ title: 'Week', href: null }} />
      <Tabs.Screen name="month" options={{ title: 'Month', href: null }} />
      <Tabs.Screen name="all" options={{ title: 'All tasks', href: null }} />
      <Tabs.Screen name="two" options={{ href: null }} />
    </Tabs>
  );
}
