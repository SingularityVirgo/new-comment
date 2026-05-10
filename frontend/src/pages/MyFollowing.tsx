import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { assetUrl, request } from '../api/request';
import type { UserDTO } from '../api/types';
import { useAuth } from '../auth/AuthContext';

export function MyFollowing() {
  const { user } = useAuth();
  const [list, setList] = useState<UserDTO[]>([]);
  const [err, setErr] = useState('');

  useEffect(() => {
    if (!user) return;
    let cancelled = false;
    void (async () => {
      const r = await request<UserDTO[]>(`/follow/following/${user.id}`);
      if (cancelled) return;
      if (!r.success) setErr(r.errorMsg || '加载失败');
      else {
        setErr('');
        setList((r.data as UserDTO[]) || []);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [user]);

  if (!user) return null;

  return (
    <>
      <h1 className="page-title">我的关注</h1>
      {err && <div className="error-banner">{err}</div>}
      {list.length === 0 && !err && <div className="card muted">暂无关注</div>}
      {list.map((u) => (
        <Link key={u.id} to={`/user/${u.id}`} className="card" style={{ display: 'block', color: 'inherit' }}>
          <div className="row" style={{ gap: 12, alignItems: 'center' }}>
            <img className="avatar" src={assetUrl(u.icon)} alt="" width={44} height={44} style={{ width: 44, height: 44 }} />
            <div style={{ flex: 1 }}>
              <div style={{ fontWeight: 600 }}>{u.nickName}</div>
              <div className="muted" style={{ fontSize: '0.85rem' }}>
                查看主页
              </div>
            </div>
          </div>
        </Link>
      ))}
    </>
  );
}
