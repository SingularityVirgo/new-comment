import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { assetUrl, request, requestJson } from '../api/request';
import type { Blog } from '../api/types';
import { useAuth } from '../auth/AuthContext';

function fmtTime(iso?: string) {
  if (!iso) return '—';
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleString('zh-CN');
}

function genderLabel(g?: boolean) {
  if (g === true) return '女';
  if (g === false) return '男';
  return '未填写';
}

export function Profile() {
  const { user, refresh } = useAuth();
  const [blogs, setBlogs] = useState<Blog[]>([]);
  const [current, setCurrent] = useState(1);
  const [signTip, setSignTip] = useState('');
  const [streak, setStreak] = useState<number | null>(null);

  const [nickEdit, setNickEdit] = useState('');
  const [nickMsg, setNickMsg] = useState('');
  const [nickBusy, setNickBusy] = useState(false);

  const [pwdCode, setPwdCode] = useState('');
  const [pwdNew, setPwdNew] = useState('');
  const [pwdNew2, setPwdNew2] = useState('');
  const [pwdMsg, setPwdMsg] = useState('');
  const [pwdCodeBusy, setPwdCodeBusy] = useState(false);
  const [pwdBusy, setPwdBusy] = useState(false);

  const [extCity, setExtCity] = useState('');
  const [extIntro, setExtIntro] = useState('');
  const [extMsg, setExtMsg] = useState('');
  const [extBusy, setExtBusy] = useState(false);

  useEffect(() => {
    void (async () => {
      const r = await request<Blog[]>('/blog/of/me', { params: { current } });
      if (r.success) setBlogs((r.data as Blog[]) || []);
    })();
  }, [current]);

  useEffect(() => {
    if (user?.nickName) setNickEdit(user.nickName);
    const info = user?.userInfo;
    setExtCity(info?.city ?? '');
    setExtIntro(info?.introduce ?? '');
  }, [user]);

  async function sign() {
    setSignTip('');
    const r = await request('/user/sign', { method: 'POST' });
    setSignTip(r.success ? '签到成功' : r.errorMsg || '签到失败');
  }

  async function loadStreak() {
    const r = await request<number>('/user/sign/count');
    if (r.success && r.data != null) setStreak(r.data);
  }

  async function saveNickname(e: React.FormEvent) {
    e.preventDefault();
    setNickMsg('');
    setNickBusy(true);
    const r = await requestJson<unknown>('/user/me/nickname', { nickName: nickEdit }, 'PUT');
    setNickBusy(false);
    if (!r.success) {
      setNickMsg(r.errorMsg || '保存失败');
      return;
    }
    setNickMsg('昵称已更新');
    await refresh();
  }

  async function sendPwdCode() {
    setPwdMsg('');
    setPwdCodeBusy(true);
    const r = await request('/user/me/code', { method: 'POST' });
    setPwdCodeBusy(false);
    setPwdMsg(r.success ? '验证码已发送到绑定手机（开发环境见后端日志）' : r.errorMsg || '发送失败');
  }

  async function savePassword(e: React.FormEvent) {
    e.preventDefault();
    setPwdMsg('');
    if (pwdNew.length < 6) {
      setPwdMsg('新密码至少 6 位');
      return;
    }
    if (pwdNew !== pwdNew2) {
      setPwdMsg('两次输入的新密码不一致');
      return;
    }
    setPwdBusy(true);
    const r = await requestJson<unknown>('/user/me/password', { newPassword: pwdNew, code: pwdCode }, 'PUT');
    setPwdBusy(false);
    if (!r.success) {
      setPwdMsg(r.errorMsg || '修改失败');
      return;
    }
    setPwdMsg('密码已更新，下次可使用密码登录');
    setPwdCode('');
    setPwdNew('');
    setPwdNew2('');
    await refresh();
  }

  async function saveExtended(e: React.FormEvent) {
    e.preventDefault();
    setExtMsg('');
    setExtBusy(true);
    const r = await requestJson<unknown>('/user/info', { city: extCity, introduce: extIntro }, 'PUT');
    setExtBusy(false);
    if (!r.success) {
      setExtMsg(r.errorMsg || '保存失败');
      return;
    }
    setExtMsg('扩展资料已保存');
    await refresh();
  }

  if (!user) return null;

  const info = user.userInfo;

  return (
    <>
      <div className="card" style={{ background: 'linear-gradient(145deg, rgba(45,212,191,0.08), rgba(167,139,250,0.06))' }}>
        <div className="row" style={{ alignItems: 'flex-start' }}>
          <img className="avatar" src={assetUrl(user.icon)} alt="" width={64} height={64} style={{ width: 64, height: 64 }} />
          <div style={{ flex: 1 }}>
            <div className="page-title" style={{ fontSize: '1.45rem', marginBottom: 4, WebkitTextFillColor: 'unset', color: 'var(--text)' }}>
              {user.nickName}
            </div>
            <div className="muted" style={{ fontSize: '0.9rem', lineHeight: 1.6 }}>
              <div>用户 ID：{user.id}</div>
              <div>手机号：{user.phoneMasked || '—'}</div>
              <div>登录密码：{user.hasPassword ? '已设置' : '未设置（请使用验证码登录后在下方设置）'}</div>
              <div>注册时间：{fmtTime(user.createTime)}</div>
              <div>资料更新时间：{fmtTime(user.updateTime)}</div>
            </div>
            <div className="row" style={{ marginTop: 12, flexWrap: 'wrap' }}>
              <Link to={`/user/${user.id}`} className="btn">
                查看我的公开主页
              </Link>
              <Link to="/profile/following" className="btn">
                我的关注
              </Link>
            </div>
            <div className="row" style={{ marginTop: 16, flexWrap: 'wrap' }}>
              <button type="button" className="btn btn-primary" onClick={() => void sign()}>
                今日签到
              </button>
              <button type="button" className="btn" onClick={() => void loadStreak()}>
                连续签到天数
              </button>
            </div>
            {signTip && (
              <div className={signTip.includes('成功') ? 'success-banner' : 'error-banner'} style={{ marginTop: 14 }}>
                {signTip}
              </div>
            )}
            {streak != null && (
              <div className="pill" style={{ marginTop: 12, display: 'inline-block' }}>
                本月连续签到 {streak} 天
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="card" style={{ marginTop: 16 }}>
        <h2 className="section-title" style={{ marginTop: 0 }}>
          扩展资料（tb_user_info）
        </h2>
        {!info && <p className="muted">暂无扩展资料记录，可在下方填写后保存创建。</p>}
        <div className="muted" style={{ fontSize: '0.9rem', lineHeight: 1.7, marginBottom: 12 }}>
          <div>城市：{info?.city || '—'}</div>
          <div>个人介绍：{info?.introduce || '—'}</div>
          <div>性别：{genderLabel(info?.gender)}</div>
          <div>生日：{info?.birthday || '—'}</div>
          <div>
            粉丝：{info?.fans ?? 0} · 关注：{info?.followee ?? 0}
          </div>
          <div>
            积分：{info?.credits ?? 0} · 会员级别：{info?.level === true ? '已开通' : info?.level === false ? '未开通' : String(info?.level ?? '—')}
          </div>
        </div>
      </div>

      <div className="card" style={{ marginTop: 16 }}>
        <h2 className="section-title" style={{ marginTop: 0 }}>
          修改昵称（tb_user.nick_name）
        </h2>
        <form onSubmit={(e) => void saveNickname(e)} className="row" style={{ flexWrap: 'wrap', gap: 10, alignItems: 'flex-end' }}>
          <div className="field" style={{ flex: '1 1 200px', marginBottom: 0 }}>
            <label htmlFor="nick">新昵称</label>
            <input id="nick" className="input" value={nickEdit} onChange={(e) => setNickEdit(e.target.value)} maxLength={32} />
          </div>
          <button type="submit" className="btn btn-primary" disabled={nickBusy}>
            {nickBusy ? '保存中…' : '保存昵称'}
          </button>
        </form>
        {nickMsg && (
          <div className={nickMsg.includes('失败') || nickMsg.includes('错误') ? 'error-banner' : 'success-banner'} style={{ marginTop: 10 }}>
            {nickMsg}
          </div>
        )}
      </div>

      <div className="card" style={{ marginTop: 16 }}>
        <h2 className="section-title" style={{ marginTop: 0 }}>
          设置 / 修改密码
        </h2>
        <p className="muted" style={{ marginTop: 0 }}>
          每次修改需向绑定手机号发送验证码并填写。密码至少 6 位。
        </p>
        <form onSubmit={(e) => void savePassword(e)}>
          <div className="row" style={{ flexWrap: 'wrap', gap: 10, marginBottom: 12 }}>
            <button type="button" className="btn" disabled={pwdCodeBusy} onClick={() => void sendPwdCode()}>
              {pwdCodeBusy ? '发送中…' : '获取手机验证码'}
            </button>
          </div>
          <div className="field">
            <label htmlFor="pwdCode">短信验证码</label>
            <input id="pwdCode" className="input" value={pwdCode} onChange={(e) => setPwdCode(e.target.value)} placeholder="6 位数字" autoComplete="one-time-code" />
          </div>
          <div className="field">
            <label htmlFor="pwd1">新密码</label>
            <input id="pwd1" type="password" className="input" value={pwdNew} onChange={(e) => setPwdNew(e.target.value)} autoComplete="new-password" />
          </div>
          <div className="field">
            <label htmlFor="pwd2">确认新密码</label>
            <input id="pwd2" type="password" className="input" value={pwdNew2} onChange={(e) => setPwdNew2(e.target.value)} autoComplete="new-password" />
          </div>
          <button type="submit" className="btn btn-primary" disabled={pwdBusy}>
            {pwdBusy ? '提交中…' : '更新密码'}
          </button>
        </form>
        {pwdMsg && (
          <div className={pwdMsg.includes('失败') || pwdMsg.includes('错误') ? 'error-banner' : 'success-banner'} style={{ marginTop: 10 }}>
            {pwdMsg}
          </div>
        )}
      </div>

      <div className="card" style={{ marginTop: 16 }}>
        <h2 className="section-title" style={{ marginTop: 0 }}>
          编辑扩展资料
        </h2>
        <form onSubmit={(e) => void saveExtended(e)}>
          <div className="field">
            <label htmlFor="city">城市</label>
            <input id="city" className="input" value={extCity} onChange={(e) => setExtCity(e.target.value)} maxLength={64} />
          </div>
          <div className="field">
            <label htmlFor="intro">个人介绍（最多 128 字）</label>
            <textarea id="intro" className="input" value={extIntro} onChange={(e) => setExtIntro(e.target.value)} rows={3} maxLength={128} style={{ resize: 'vertical' }} />
          </div>
          <button type="submit" className="btn btn-primary" disabled={extBusy}>
            {extBusy ? '保存中…' : '保存扩展资料'}
          </button>
        </form>
        {extMsg && (
          <div className={extMsg.includes('失败') || extMsg.includes('错误') ? 'error-banner' : 'success-banner'} style={{ marginTop: 10 }}>
            {extMsg}
          </div>
        )}
      </div>

      <h2 className="section-title">我的笔记</h2>
      {blogs.map((b) => (
        <Link key={b.id} to={`/blog/${b.id}`} className="card" style={{ display: 'block', color: 'inherit' }}>
          <div className="feed-title">{b.title}</div>
          <div className="muted" style={{ marginTop: 6 }}>
            赞 {b.liked}
          </div>
        </Link>
      ))}
      {blogs.length >= 10 && (
        <div className="pager">
          <button type="button" className="btn" disabled={current <= 1} onClick={() => setCurrent((c) => Math.max(1, c - 1))}>
            上一页
          </button>
          <button type="button" className="btn" onClick={() => setCurrent((c) => c + 1)}>
            下一页
          </button>
        </div>
      )}
    </>
  );
}
