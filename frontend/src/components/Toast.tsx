import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';

type UndoFn = () => void;

type ToastPayload = {
  message: string;
  undo?: UndoFn;
  /** 撤销可用时长（秒） */
  undoSeconds?: number;
};

type Ctx = {
  showToast: (p: string | ToastPayload) => void;
};

const ToastContext = createContext<Ctx | null>(null);

type Item = {
  id: string;
  message: string;
  undo?: UndoFn;
  undoSeconds: number;
};

function ToastRow({
  item,
  onDismiss,
}: {
  item: Item;
  onDismiss: (id: string) => void;
}) {
  const [left, setLeft] = useState(item.undoSeconds);

  useEffect(() => {
    if (!item.undo) {
      const t = window.setTimeout(() => onDismiss(item.id), 3200);
      return () => window.clearTimeout(t);
    }
    if (left <= 0) {
      onDismiss(item.id);
      return;
    }
    const t = window.setTimeout(() => setLeft((x) => x - 1), 1000);
    return () => window.clearTimeout(t);
  }, [item.id, item.undo, left, onDismiss]);

  return (
    <div className="toast-item" role="status">
      <span className="toast-msg">{item.message}</span>
      {item.undo && left > 0 && (
        <button
          type="button"
          className="btn btn-ghost toast-undo"
          onClick={() => {
            try {
              item.undo?.();
            } finally {
              onDismiss(item.id);
            }
          }}
        >
          撤销（{left}s）
        </button>
      )}
    </div>
  );
}

export function ToastProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<Item[]>([]);

  const dismiss = useCallback((id: string) => {
    setItems((xs) => xs.filter((x) => x.id !== id));
  }, []);

  const showToast = useCallback((p: string | ToastPayload) => {
    const payload = typeof p === 'string' ? { message: p } : p;
    const id = `${Date.now()}-${Math.random().toString(36).slice(2, 7)}`;
    const undoSeconds = payload.undo ? Math.max(1, payload.undoSeconds ?? 3) : 0;
    setItems((xs) => [
      ...xs,
      {
        id,
        message: payload.message,
        undo: payload.undo,
        undoSeconds: payload.undo ? undoSeconds : 0,
      },
    ]);
  }, []);

  const value = useMemo(() => ({ showToast }), [showToast]);

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="toast-host" aria-live="polite" aria-relevant="additions text">
        {items.map((t) => (
          <ToastRow key={t.id} item={t} onDismiss={dismiss} />
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const v = useContext(ToastContext);
  if (!v) throw new Error('useToast outside ToastProvider');
  return v;
}
