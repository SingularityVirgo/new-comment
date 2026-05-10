import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { request, requestJson } from '../api/request';
import { useAuth } from '../auth/AuthContext';

export function Login() {
  const [phone, setPhone] = useState('');
  const [code, setCode] = useState('');
  const [msg, setMsg] = useState('');
  const [sending, setSending] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const nav = useNavigate();
  const loc = useLocation();
  const { refresh, setToken } = useAuth();

  const from = (loc.state as { from?: string } | null)?.from || '/discover';

  async function sendCode() {
    setMsg('');
    setSending(true);
    const r = await request('/user/code', { method: 'POST', params: { phone } });
    setSending(false);
    if (!r.success) setMsg(r.errorMsg || '发送失败');
    else setMsg('验证码已发送（开发环境请查看后端日志中的验证码）');
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setMsg('');
    setSubmitting(true);
    const r = await requestJson<string>('/user/login', { phone, code });
    setSubmitting(false);
    if (!r.success || r.data == null) {
      setMsg(r.errorMsg || '登录失败');
      return;
    }
    setToken(r.data);
    await refresh();
    nav(from, { replace: true });
  }

  const msgIsError = msg.includes('失败') || msg.includes('错误');

  return (
    <div className="login-shell">
      <header className="page-head" style={{ textAlign: 'center' }}>
        <h1 className="page-title">欢迎回来</h1>
        <p className="page-lead" style={{ margin: '0 auto' }}>
          手机号 + 验证码登录，与后端 Redis 校验一致。
        </p>
      </header>
      <div className="login-card">
        <div className="login-card-inner">
          <h2 style={{ margin: '0 0 6px', fontSize: '1.15rem' }}>登录</h2>
          <p className="muted" style={{ marginTop: 0, marginBottom: 18 }}>
            新用户首次登录将自动注册
          </p>
          {msg &&
            (msgIsError ? (
              <div className="error-banner">{msg}</div>
            ) : (
              <div className="success-banner">{msg}</div>
            ))}
          <form onSubmit={onSubmit}>
            <div className="field">
              <label htmlFor="phone">手机号</label>
              <input
                id="phone"
                className="input"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="11 位手机号"
                autoComplete="tel"
              />
            </div>
            <div className="field">
              <label htmlFor="code">验证码</label>
              <div className="row" style={{ flexWrap: 'wrap' }}>
                <input
                  id="code"
                  className="input"
                  style={{ flex: 1, minWidth: 120 }}
                  value={code}
                  onChange={(e) => setCode(e.target.value)}
                  placeholder="6 位数字"
                />
                <button type="button" className="btn" disabled={sending} onClick={() => void sendCode()}>
                  {sending ? '发送中…' : '获取验证码'}
                </button>
              </div>
            </div>
            <button type="submit" className="btn btn-primary" disabled={submitting} style={{ width: '100%', marginTop: 10 }}>
              {submitting ? '登录中…' : '进入应用'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
