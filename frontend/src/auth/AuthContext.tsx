import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import { request } from '../api/request';
import type { UserDTO } from '../api/types';

const AUTH_BC = 'mzy-comment-auth';

type AuthCtx = {
  user: UserDTO | null;
  loading: boolean;
  refresh: () => Promise<void>;
  setToken: (token: string | null) => void;
  logout: () => void;
};

const Ctx = createContext<AuthCtx | null>(null);

function tabId(): string {
  const w = window as Window & { __mzyTabId?: string };
  if (!w.__mzyTabId) w.__mzyTabId = `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`;
  return w.__mzyTabId;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const bcRef = useRef<BroadcastChannel | null>(null);

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

  useEffect(() => {
    if (typeof BroadcastChannel === 'undefined') return;
    const ch = new BroadcastChannel(AUTH_BC);
    bcRef.current = ch;
    ch.onmessage = (ev: MessageEvent<{ from?: string; token: string | null }>) => {
      const d = ev.data;
      if (!d || d.from === tabId()) return;
      if (d.token) localStorage.setItem('token', d.token);
      else localStorage.removeItem('token');
      window.dispatchEvent(new Event('auth-change'));
    };
    return () => {
      ch.close();
      bcRef.current = null;
    };
  }, []);

  const broadcastToken = useCallback((token: string | null) => {
    try {
      bcRef.current?.postMessage({ from: tabId(), token });
    } catch {
      /* ignore */
    }
  }, []);

  const setToken = useCallback(
    (token: string | null) => {
      if (token) localStorage.setItem('token', token);
      else localStorage.removeItem('token');
      broadcastToken(token);
      window.dispatchEvent(new Event('auth-change'));
    },
    [broadcastToken],
  );

  const logout = useCallback(() => {
    void request('/user/logout', { method: 'POST' });
    localStorage.removeItem('token');
    broadcastToken(null);
    setUser(null);
    window.dispatchEvent(new Event('auth-change'));
  }, [broadcastToken]);

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
