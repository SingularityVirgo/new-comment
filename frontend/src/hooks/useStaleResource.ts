import { useCallback, useEffect, useRef, useState } from 'react';

type Options = {
  /** stale-while-revalidate：数据在多久内视为新鲜，超时后后台刷新 */
  staleMs?: number;
  /** 后台轮询间隔（仅在线时）；0 表示不轮询 */
  revalidateIntervalMs?: number;
};

type State<T> = {
  data: T | undefined;
  error: string;
  isLoading: boolean;
  isValidating: boolean;
};

/**
 * 轻量 SWR：展示缓存数据的同时后台 revalidate；适合列表/详情首屏。
 */
export function useStaleResource<T>(
  key: string,
  fetcher: () => Promise<T>,
  opts: Options = {},
) {
  const staleMs = opts.staleMs ?? 8_000;
  const revalidateIntervalMs = opts.revalidateIntervalMs ?? 0;

  const cacheRef = useRef<Map<string, { at: number; data: T }>>(new Map());
  const [state, setState] = useState<State<T>>({
    data: undefined,
    error: '',
    isLoading: true,
    isValidating: false,
  });

  const revalidate = useCallback(
    async (background: boolean) => {
      const cached = cacheRef.current.get(key);
      const fresh = cached && Date.now() - cached.at < staleMs;
      if (background && fresh) return;

      setState((s) => ({
        ...s,
        isLoading: !cached && !s.data && !background,
        isValidating: !!background && (!!cached || !!s.data),
        error: '',
      }));

      try {
        const data = await fetcher();
        cacheRef.current.set(key, { at: Date.now(), data });
        setState({
          data,
          error: '',
          isLoading: false,
          isValidating: false,
        });
      } catch (e) {
        const msg = e instanceof Error ? e.message : '加载失败';
        setState((s) => ({
          ...s,
          error: msg,
          isLoading: false,
          isValidating: false,
        }));
      }
    },
    [key, fetcher, staleMs],
  );

  useEffect(() => {
    const cached = cacheRef.current.get(key);
    if (cached) {
      setState({
        data: cached.data,
        error: '',
        isLoading: false,
        isValidating: false,
      });
      void revalidate(true);
    } else {
      setState({ data: undefined, error: '', isLoading: true, isValidating: false });
      void revalidate(false);
    }
  }, [key, revalidate]);

  useEffect(() => {
    if (!revalidateIntervalMs || typeof navigator === 'undefined' || !navigator.onLine) return;
    const id = window.setInterval(() => {
      void revalidate(true);
    }, revalidateIntervalMs);
    return () => window.clearInterval(id);
  }, [key, revalidateIntervalMs, revalidate]);

  useEffect(() => {
    const onFocus = () => void revalidate(true);
    window.addEventListener('focus', onFocus);
    return () => window.removeEventListener('focus', onFocus);
  }, [revalidate]);

  return { ...state, revalidate: () => void revalidate(false) };
}
