import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { request, requestJson } from '../api/request';
import { useAuth } from '../auth/AuthContext';
import { useDebouncedSubmit } from '../hooks/useDebouncedSubmit';

type LoginMode = 'code' | 'password';

export function Login() {
  const [mode, setMode] = useState<LoginMode>('code');
  const [phone, setPhone] = useState('');
  const [code, setCode] = useState('');
  const [password, setPassword] = useState('');
  const [msg, setMsg] = useState('');
  const [msgOk, setMsgOk] = useState(false);
  const [sending, setSending] = useState(false);
  const { locked: submitLocked, run: runLogin } = useDebouncedSubmit();
  const nav = useNavigate();
  const loc = useLocation();
  const { refresh, setToken } = useAuth();

  const from = (loc.state as { from?: string } | null)?.from || '/discover';

  function setBanner(text: string, ok: boolean) {
    setMsg(text);
    setMsgOk(ok);
  }

  async function sendCode() {
    setBanner('', false);
    setSending(true);
    const r = await request('/user/code', { method: 'POST', params: { phone } });
    setSending(false);
    if (!r.success) setBanner(r.errorMsg || '发送失败', false);
    else setBanner('验证码已发送（开发环境请查看后端日志中的验证码）', true);
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setBanner('', false);
    try {
      await runLogin(async () => {
        const r =
          mode === 'code'
            ? await requestJson<string>('/user/login', { phone, code })
            : await requestJson<string>('/user/login', { phone, password });
        if (!r.success || r.data == null) {
          setBanner(r.errorMsg || '登录失败', false);
          return;
        }
        setToken(r.data);
        await refresh();
        nav(from, { replace: true });
      });
    } catch {
      /* 提交冷却中 */
    }
  }

  return (
    <div className="login-shell">
      <header className="page-head" style={{ textAlign: 'center' }}>
        <h1 className="page-title">欢迎回来</h1>
        <p className="page-lead" style={{ margin: '0 auto' }}>
          支持验证码登录或密码登录；未设置过密码的账号请使用手机号验证码登录。
        </p>
      </header>
      <div className="login-card">
        <div className="login-card-inner">
          <h2 style={{ margin: '0 0 6px', fontSize: '1.15rem' }}>登录</h2>
          <p className="muted" style={{ marginTop: 0, marginBottom: 14 }}>
            新用户首次使用验证码登录将自动注册
          </p>
          <div className="row" style={{ gap: 8, marginBottom: 16, flexWrap: 'wrap' }}>
            <button
              type="button"
              className={mode === 'code' ? 'btn btn-primary' : 'btn'}
              style={{ flex: 1, minWidth: 120 }}
              onClick={() => {
                setMode('code');
                setBanner('', false);
              }}
            >
              验证码登录
            </button>
            <button
              type="button"
              className={mode === 'password' ? 'btn btn-primary' : 'btn'}
              style={{ flex: 1, minWidth: 120 }}
              onClick={() => {
                setMode('password');
                setBanner('', false);
              }}
            >
              密码登录
            </button>
          </div>
          {msg &&
            (msgOk ? (
              <div className="success-banner" style={{ marginBottom: 12 }}>
                {msg}
              </div>
            ) : (
              <div className="error-banner" style={{ marginBottom: 12 }}>
                {msg}
              </div>
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
            {mode === 'code' ? (
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
                    autoComplete="one-time-code"
                  />
                  <button type="button" className="btn" disabled={sending} onClick={() => void sendCode()}>
                    {sending ? '发送中…' : '获取验证码'}
                  </button>
                </div>
              </div>
            ) : (
              <div className="field">
                <label htmlFor="password">密码</label>
                <input
                  id="password"
                  type="password"
                  className="input"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="已设置的登录密码"
                  autoComplete="current-password"
                />
              </div>
            )}
            <button type="submit" className="btn btn-primary" disabled={submitLocked} style={{ width: '100%', marginTop: 10 }}>
              {submitLocked ? '登录中…' : '进入应用'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
