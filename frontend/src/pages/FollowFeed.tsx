import { useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { assetUrl, request } from '../api/request';
import type { Blog, ScrollResult } from '../api/types';

const FIRST_LAST_ID = '9223372036854775807';

export function FollowFeed() {
  const [blogs, setBlogs] = useState<Blog[]>([]);
  const [done, setDone] = useState(false);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState('');
  const cursor = useRef({ lastId: FIRST_LAST_ID, offset: 0 });

  async function load() {
    if (done || loading) return;
    setLoading(true);
    setErr('');
    const { lastId, offset } = cursor.current;
    const r = await request<ScrollResult>('/blog/of/follow', {
      params: { lastId, offset: offset || undefined },
    });
    setLoading(false);
    if (!r.success) {
      setErr(r.errorMsg || '加载失败');
      return;
    }
    const data = r.data;
    if (!data || !data.list || data.list.length === 0) {
      setDone(true);
      return;
    }
    setBlogs((prev) => [...prev, ...data.list]);
    cursor.current = { lastId: String(data.minTime), offset: data.offset ?? 0 };
  }

  return (
    <>
      <h1 style={{ fontSize: '1.25rem', margin: '0 0 12px' }}>关注动态</h1>
      <p className="muted">展示你关注的人发布的笔记（与后端 `ScrollResult` 滚动分页一致）。</p>
      {err && <div className="error-banner">{err}</div>}
      {blogs.length === 0 && !done && (
        <button type="button" className="btn btn-primary" onClick={() => void load()} disabled={loading}>
          {loading ? '加载中…' : '加载动态'}
        </button>
      )}
      {blogs.map((b) => (
        <Link key={`${b.id}-${b.createTime ?? ''}`} to={`/blog/${b.id}`} className="card" style={{ display: 'flex', gap: 12, color: 'inherit' }}>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontWeight: 600 }}>{b.title}</div>
            <div className="muted">
              {b.name} · 赞 {b.liked}
            </div>
          </div>
          {b.images?.split(',')[0] && (
            <img
              src={assetUrl(b.images.split(',')[0].trim())}
              alt=""
              style={{ width: 72, height: 72, borderRadius: 8, objectFit: 'cover' }}
            />
          )}
        </Link>
      ))}
      {blogs.length > 0 && !done && (
        <button type="button" className="btn" style={{ width: '100%', marginTop: 8 }} disabled={loading} onClick={() => void load()}>
          {loading ? '加载中…' : '加载更多'}
        </button>
      )}
      {done && blogs.length > 0 && <p className="muted">没有更多了</p>}
      {done && blogs.length === 0 && <p className="muted">暂无动态，先去关注一些用户吧。</p>}
    </>
  );
}
