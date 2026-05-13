import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { assetUrl, request } from '../api/request';
import type { Blog, UserDTO, UserInfoDTO } from '../api/types';
import { useAuth } from '../auth/AuthContext';

export function UserPage() {
  const { id } = useParams();
  const uid = Number(id);
  const { user: me } = useAuth();
  const [profile, setProfile] = useState<UserDTO | null>(null);
  const [blogs, setBlogs] = useState<Blog[]>([]);
  const [following, setFollowing] = useState<boolean | null>(null);
  const [followingList, setFollowingList] = useState<UserDTO[]>([]);
  const [current, setCurrent] = useState(1);
  const [err, setErr] = useState('');
  const [userInfo, setUserInfo] = useState<UserInfoDTO | null | undefined>(undefined);

  function fmtTime(iso?: string) {
    if (!iso) return '—';
    const d = new Date(iso);
    return Number.isNaN(d.getTime()) ? iso : d.toLocaleString('zh-CN');
  }

  function genderLabel(g?: boolean) {
    if (g === true) return '女';
    if (g === false) return '男';
    return '—';
  }

  useEffect(() => {
    if (!Number.isFinite(uid)) return;
    let cancelled = false;
    setUserInfo(undefined);
    void (async () => {
      const u = await request<UserDTO>(`/user/${uid}`);
      if (cancelled) return;
      if (u.success) setProfile(u.data ?? null);
      const ui = await request<UserInfoDTO | null>(`/user/info/${uid}`);
      if (cancelled) return;
      const loadedInfo = ui.success ? ((ui.data as UserInfoDTO | null) ?? null) : null;
      if (ui.success) setUserInfo(loadedInfo);
      else setUserInfo(null);
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
      if (me) {
        const hiddenFromViewer = me.id !== uid && loadedInfo?.hideFollowing === true;
        if (!hiddenFromViewer) {
          const fl = await request<UserDTO[]>(`/follow/following/${uid}`);
          if (!cancelled && fl.success) setFollowingList((fl.data as UserDTO[]) || []);
        } else if (!cancelled) {
          setFollowingList([]);
        }
      } else if (!cancelled) {
        setFollowingList([]);
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

  async function followUserInList(targetId: number) {
    if (!me || targetId === me.id) return;
    const r = await request(`/follow/${targetId}/true`, { method: 'PUT' });
    if (!r.success) {
      alert(r.errorMsg || '关注失败');
      return;
    }
    setFollowingList((prev) => prev.map((u) => (u.id === targetId ? { ...u, isFollow: true } : u)));
  }

  if (!Number.isFinite(uid)) return <div className="error-banner">无效用户</div>;

  return (
    <>
      <div className="card" style={{ background: 'linear-gradient(145deg, rgba(167,139,250,0.08), rgba(45,212,191,0.05))' }}>
        <div className="row" style={{ alignItems: 'flex-start' }}>
          <img className="avatar" src={assetUrl(profile?.icon)} alt="" width={64} height={64} style={{ width: 64, height: 64 }} />
          <div style={{ flex: 1 }}>
            <div className="page-title" style={{ fontSize: '1.4rem', marginBottom: 4, WebkitTextFillColor: 'unset', color: 'var(--text)' }}>
              {profile?.nickName ?? `用户 ${uid}`}
            </div>
            <div className="muted" style={{ fontSize: '0.88rem', lineHeight: 1.65, marginTop: 8 }}>
              <div>用户 ID：{profile?.id ?? uid}</div>
              <div>注册时间：{fmtTime(profile?.createTime)}</div>
              <div>资料更新时间：{fmtTime(profile?.updateTime)}</div>
            </div>
            {me && me.id !== uid && (
              <div className="row" style={{ marginTop: 12, flexWrap: 'wrap' }}>
                <button type="button" className="btn btn-primary" onClick={() => void toggleFollow(!(following ?? false))}>
                  {following ? '取消关注' : '关注'}
                </button>
              </div>
            )}
          </div>
        </div>
        <div style={{ marginTop: 16, paddingTop: 16, borderTop: '1px solid var(--stroke)' }}>
          <div className="muted" style={{ marginBottom: 10, fontWeight: 600 }}>
            关注列表
          </div>
          {me?.id !== uid && userInfo?.hideFollowing === true ? (
            <span className="muted">该用户已隐藏关注列表，仅本人可见。</span>
          ) : followingList.length === 0 ? (
            <span className="muted">暂无</span>
          ) : (
            followingList.map((u) => (
            <div
              key={u.id}
              className="row"
              style={{
                justifyContent: 'space-between',
                alignItems: 'center',
                gap: 10,
                padding: '8px 0',
                borderBottom: '1px solid var(--stroke)',
              }}
            >
              <Link to={`/user/${u.id}`} className="row" style={{ gap: 10, flex: 1, minWidth: 0, color: 'inherit' }}>
                <img className="avatar" src={assetUrl(u.icon)} alt="" width={36} height={36} style={{ width: 36, height: 36, flexShrink: 0 }} />
                <span style={{ color: 'var(--text)', fontSize: '0.95rem', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {u.nickName}
                </span>
              </Link>
              {me && u.id !== me.id ? (
                u.isFollow ? (
                  <span className="muted" style={{ fontSize: '0.85rem', flexShrink: 0 }}>
                    已关注
                  </span>
                ) : (
                  <button type="button" className="btn btn-primary" style={{ padding: '4px 12px', minWidth: 40 }} onClick={() => void followUserInList(u.id)} aria-label={`关注 ${u.nickName}`}>
                    +
                  </button>
                )
              ) : null}
            </div>
            ))
          )}
        </div>
      </div>

      <div className="card" style={{ marginTop: 14 }}>
        <div className="muted" style={{ marginBottom: 8, fontWeight: 600 }}>
          扩展资料（tb_user_info）
        </div>
        {userInfo === undefined && <span className="muted">加载中…</span>}
        {userInfo === null && <span className="muted">暂无扩展资料（表中无记录）</span>}
        {userInfo != null && (
          <div className="muted" style={{ fontSize: '0.9rem', lineHeight: 1.7 }}>
            <div>城市：{userInfo.city || '—'}</div>
            <div>个人介绍：{userInfo.introduce || '—'}</div>
            <div>性别：{genderLabel(userInfo.gender)}</div>
            <div>生日：{userInfo.birthday || '—'}</div>
            <div>
              粉丝：{userInfo.fans ?? 0} · 关注：{userInfo.followee ?? 0}
            </div>
            <div>
              积分：{userInfo.credits ?? 0} · 会员：{userInfo.level === true ? '已开通' : userInfo.level === false ? '未开通' : String(userInfo.level ?? '—')}
            </div>
            <div>关注列表：{userInfo.hideFollowing ? '仅本人可见（已隐藏）' : '公开'}</div>
          </div>
        )}
      </div>

      {err && <div className="error-banner">{err}</div>}
      <h2 className="section-title">TA 的笔记</h2>
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
          <span className="muted">第 {current} 页</span>
          <button type="button" className="btn" onClick={() => setCurrent((c) => c + 1)}>
            下一页
          </button>
        </div>
      )}
    </>
  );
}
