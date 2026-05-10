import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { request } from '../api/request';
import type { UserDTO } from '../api/types';

type AuthCtx = {
  user: UserDTO | null;
  loading: boolean;
  refresh: () => Promise<void>;
  setToken: (token: string | null) => void;
  logout: () => void;
};

const Ctx = createContext<AuthCtx | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserDTO | null>(null);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      setUser(null);
      setLoading(false);
      return;
    }
    const r = await request<UserDTO>('/user/me');
    if (r.success && r.data) setUser(r.data);
    else setUser(null);
    setLoading(false);
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  useEffect(() => {
    const onAuth = () => void refresh();
    window.addEventListener('auth-change', onAuth);
    return () => window.removeEventListener('auth-change', onAuth);
  }, [refresh]);

  const setToken = useCallback((token: string | null) => {
    if (token) localStorage.setItem('token', token);
    else localStorage.removeItem('token');
    window.dispatchEvent(new Event('auth-change'));
  }, []);

  const logout = useCallback(() => {
    void request('/user/logout', { method: 'POST' });
    localStorage.removeItem('token');
    setUser(null);
    window.dispatchEvent(new Event('auth-change'));
  }, []);

  const value = useMemo(
    () => ({ user, loading, refresh, setToken, logout }),
    [user, loading, refresh, setToken, logout],
  );

  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export function useAuth() {
  const v = useContext(Ctx);
  if (!v) throw new Error('useAuth outside AuthProvider');
  return v;
}
