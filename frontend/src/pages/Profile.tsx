import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { assetUrl, request } from '../api/request';
import type { Blog } from '../api/types';
import { useAuth } from '../auth/AuthContext';

export function Profile() {
  const { user } = useAuth();
  const [blogs, setBlogs] = useState<Blog[]>([]);
  const [current, setCurrent] = useState(1);
  const [signTip, setSignTip] = useState('');
  const [streak, setStreak] = useState<number | null>(null);

  useEffect(() => {
    void (async () => {
      const r = await request<Blog[]>('/blog/of/me', { params: { current } });
      if (r.success) setBlogs((r.data as Blog[]) || []);
    })();
  }, [current]);

  async function sign() {
    setSignTip('');
    const r = await request('/user/sign', { method: 'POST' });
    setSignTip(r.success ? '签到成功' : r.errorMsg || '签到失败');
  }

  async function loadStreak() {
    const r = await request<number>('/user/sign/count');
    if (r.success && r.data != null) setStreak(r.data);
  }

  if (!user) return null;

  return (
    <>
      <div className="card">
        <div className="row">
          <img className="avatar" src={assetUrl(user.icon)} alt="" width={56} height={56} />
          <div>
            <div style={{ fontWeight: 700 }}>{user.nickName}</div>
            <div className="muted">ID：{user.id}</div>
          </div>
        </div>
        <div className="row" style={{ marginTop: 12, flexWrap: 'wrap' }}>
          <button type="button" className="btn btn-primary" onClick={() => void sign()}>
            今日签到
          </button>
          <button type="button" className="btn" onClick={() => void loadStreak()}>
            连续签到天数
          </button>
        </div>
        {signTip && <div className="muted" style={{ marginTop: 8 }}>{signTip}</div>}
        {streak != null && <div className="muted" style={{ marginTop: 4 }}>本月连续签到：{streak} 天</div>}
      </div>
      <h2 style={{ fontSize: '1.05rem' }}>我的笔记</h2>
      {blogs.map((b) => (
        <Link key={b.id} to={`/blog/${b.id}`} className="card" style={{ display: 'block', color: 'inherit' }}>
          <div style={{ fontWeight: 600 }}>{b.title}</div>
          <div className="muted">赞 {b.liked}</div>
        </Link>
      ))}
      {blogs.length >= 10 && (
        <div className="row" style={{ justifyContent: 'center' }}>
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
