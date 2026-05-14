import { Component, type ErrorInfo, type ReactNode } from 'react';

type Props = { children: ReactNode };
type State = { hasError: boolean; message: string };

/**
 * 错误边界：隔离子树故障，避免整页白屏（优雅降级）。
 */
export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, message: '' };
  }

  static getDerivedStateFromError(err: Error): State {
    return { hasError: true, message: err.message || '渲染出错' };
  }

  componentDidCatch(err: Error, info: ErrorInfo) {
    console.error('[ErrorBoundary]', err, info.componentStack);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="error-boundary card" role="alert">
          <h2 className="error-boundary-title">页面遇到问题</h2>
          <p className="muted">{this.state.message}</p>
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => this.setState({ hasError: false, message: '' })}
          >
            重试
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}
