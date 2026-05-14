import { useCallback, useRef, useState } from 'react';

const DEFAULT_COOLDOWN_MS = 500;

/**
 * 提交后短窗口内禁用，防止重复请求（幂等前端控制）。
 */
export function useDebouncedSubmit(cooldownMs = DEFAULT_COOLDOWN_MS) {
  const busy = useRef(false);
  const timer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const [locked, setLocked] = useState(false);

  const run = useCallback(
    async <T>(fn: () => Promise<T>): Promise<T> => {
      if (busy.current) return Promise.reject(new Error('submit-cooldown'));
      busy.current = true;
      setLocked(true);
      if (timer.current) clearTimeout(timer.current);
      try {
        return await fn();
      } finally {
        timer.current = setTimeout(() => {
          busy.current = false;
          setLocked(false);
          timer.current = null;
        }, cooldownMs);
      }
    },
    [cooldownMs],
  );

  return { locked, run };
}
