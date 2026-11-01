import { createContext, PropsWithChildren, useContext, useEffect, useMemo, useState } from 'react';
import { ApiError } from '../api/errors';
import { authApi } from '../api/authApi';
import { clearAuthToken, getAuthToken, setUnauthorizedHandler } from '../api/httpClient';
import { userApi } from '../api/userApi';
import { LoginRequest, User } from '../api/models';

type AuthContextValue = {
  user: User | null;
  loading: boolean;
  error: ApiError | null;
  signIn: (credentials: LoginRequest) => Promise<void>;
  signOut: () => Promise<void>;
  clearError: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ApiError | null>(null);

  useEffect(() => {
    let active = true;
    const handleUnauthorized = () => {
      if (active) {
        setUser(null);
        setError(null);
      }
    };
    setUnauthorizedHandler(handleUnauthorized);
    void (async () => {
      try {
        if (await getAuthToken()) {
          const currentUser = await userApi.me();
          if (active) setUser(currentUser);
        }
      } catch (requestError) {
        if (active && requestError instanceof ApiError && requestError.status !== 401) {
          setError(requestError);
        }
      } finally {
        if (active) setLoading(false);
      }
    })();
    return () => {
      active = false;
      setUnauthorizedHandler(undefined);
    };
  }, []);

  const value = useMemo<AuthContextValue>(() => ({
    user,
    loading,
    error,
    clearError: () => setError(null),
    signIn: async (credentials) => {
      setLoading(true);
      setError(null);
      try {
        await authApi.login(credentials);
        setUser(await userApi.me());
      } catch (requestError) {
        const authError = requestError instanceof ApiError ? requestError : new ApiError(0, 'Unable to sign in.');
        setError(authError);
        throw authError;
      } finally {
        setLoading(false);
      }
    },
    signOut: async () => {
      await clearAuthToken();
      setUser(null);
      setError(null);
    },
  }), [error, loading, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
