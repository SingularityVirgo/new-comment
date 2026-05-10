import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { assetUrl, request } from '../api/request';
import type { Blog, UserDTO } from '../api/types';
import { useAuth } from '../auth/AuthContext';

export function UserPage() {
  const { id } = useParams();
  const uid = Number(id);
  const { user: me } = useAuth();
  const [profile, setProfile] = useState<UserDTO | null>(null);
  const [blogs, setBlogs] = useState<Blog[]>([]);
  const [following, setFollowing] = useState<boolean | null>(null);
  const [commons, setCommons] = useState<UserDTO[] | null>(null);
  const [current, setCurrent] = useState(1);
  const [err, setErr] = useState('');

  useEffect(() => {
    if (!Number.isFinite(uid)) return;
    let cancelled = false;
    void (async () => {
      const u = await request<UserDTO>(`/user/${uid}`);
      if (cancelled) return;
      if (u.success) setProfile(u.data ?? null);
      const b = await request<Blog[]>('/blog/of/user', { params: { id: uid, current } });
      if (cancelled) return;
      if (!b.success) setErr(b.errorMsg || '加载失败');
      else {
        setErr('');
        setBlogs((b.data as Blog[]) || []);
      }
      if (me && me.id !== uid) {
        const f = await request<boolean>(`/follow/or/not/${uid}`);
        if (!cancelled && f.success) setFollowing(!!f.data);
      } else {
        setFollowing(null);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [uid, current, me]);

  async function toggleFollow(follow: boolean) {
    if (!me) return;
    const r = await request(`/follow/${uid}/${follow}`, { method: 'PUT' });
    if (!r.success) {
      alert(r.errorMsg || '操作失败');
      return;
    }
    setFollowing(follow);
  }

  async function loadCommons() {
    const r = await request<UserDTO[]>(`/follow/common/${uid}`);
    if (r.success) setCommons((r.data as UserDTO[]) || []);
  }

  if (!Number.isFinite(uid)) return <div className="error-banner">无效用户</div>;

  return (
    <>
      <div className="card">
        <div className="row">
          <img className="avatar" src={assetUrl(profile?.icon)} alt="" width={56} height={56} />
          <div>
            <div style={{ fontWeight: 700, fontSize: '1.1rem' }}>{profile?.nickName ?? `用户 ${uid}`}</div>
            {me && me.id !== uid && (
              <div className="row" style={{ marginTop: 8 }}>
                <button type="button" className="btn btn-primary" onClick={() => void toggleFollow(!(following ?? false))}>
                  {following ? '取消关注' : '关注'}
                </button>
                <button type="button" className="btn" onClick={() => void loadCommons()}>
                  共同关注
                </button>
              </div>
            )}
          </div>
        </div>
        {commons && (
          <div style={{ marginTop: 12 }}>
            <div className="muted" style={{ marginBottom: 6 }}>
              共同关注
            </div>
            <div className="row" style={{ flexWrap: 'wrap' }}>
              {commons.length === 0 && <span className="muted">无</span>}
              {commons.map((u) => (
                <Link key={u.id} to={`/user/${u.id}`} className="row" style={{ gap: 4 }}>
                  <img className="avatar" src={assetUrl(u.icon)} alt="" width={28} height={28} />
                  {u.nickName}
                </Link>
              ))}
            </div>
          </div>
        )}
      </div>
      {err && <div className="error-banner">{err}</div>}
      <h2 style={{ fontSize: '1.05rem' }}>TA 的笔记</h2>
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
          <span className="muted">第 {current} 页</span>
          <button type="button" className="btn" onClick={() => setCurrent((c) => c + 1)}>
            下一页
          </button>
        </div>
      )}
    </>
  );
}
