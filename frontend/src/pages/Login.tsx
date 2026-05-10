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

  return (
    <div className="card" style={{ maxWidth: 400, margin: '0 auto' }}>
      <h2 style={{ marginTop: 0 }}>手机号登录</h2>
      <p className="muted">与后端一致：使用 Redis 中的验证码校验。</p>
      {msg && <div className={msg.includes('失败') || msg.includes('错误') ? 'error-banner' : 'card muted'}>{msg}</div>}
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
        <button type="submit" className="btn btn-primary" disabled={submitting} style={{ width: '100%', marginTop: 8 }}>
          {submitting ? '登录中…' : '登录'}
        </button>
      </form>
    </div>
  );
}
