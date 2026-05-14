type Props = {
  title: string;
  description: string;
  actionLabel?: string;
  onAction?: () => void;
};

/**
 * 空状态模式：引导用户下一步，而非留白。
 */
export function EmptyState({ title, description, actionLabel, onAction }: Props) {
  return (
    <div className="empty-state card" role="status">
      <div className="empty-state-icon" aria-hidden>
        ◇
      </div>
      <h2 className="empty-state-title">{title}</h2>
      <p className="empty-state-desc">{description}</p>
      {actionLabel && onAction && (
        <button type="button" className="btn btn-primary" onClick={onAction}>
          {actionLabel}
        </button>
      )}
    </div>
  );
}
