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
      <header className="page-head">
        <h1 className="page-title">关注动态</h1>
        <p className="page-lead">你关注的人发布的笔记，滚动加载与后端 ScrollResult 一致。</p>
      </header>
      {err && <div className="error-banner">{err}</div>}
      {blogs.length === 0 && !done && (
        <button type="button" className="btn btn-primary" style={{ padding: '12px 28px' }} onClick={() => void load()} disabled={loading}>
          {loading ? (
            <span className="row" style={{ gap: 10 }}>
              <span className="spinner" aria-hidden />
              加载中…
            </span>
          ) : (
            '加载动态'
          )}
        </button>
      )}
      {blogs.map((b) => (
        <Link key={`${b.id}-${b.createTime ?? ''}`} to={`/blog/${b.id}`} className="card" style={{ display: 'flex', gap: 14, color: 'inherit', alignItems: 'center' }}>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div className="feed-title">{b.title}</div>
            <div className="muted" style={{ marginTop: 4 }}>
              {b.name} · 赞 {b.liked}
            </div>
          </div>
          {b.images?.split(',')[0] && (
            <img className="feed-thumb" src={assetUrl(b.images.split(',')[0].trim())} alt="" />
          )}
        </Link>
      ))}
      {blogs.length > 0 && !done && (
        <button type="button" className="btn" style={{ width: '100%', marginTop: 12 }} disabled={loading} onClick={() => void load()}>
          {loading ? (
            <span className="row" style={{ gap: 10 }}>
              <span className="spinner" aria-hidden />
              加载中…
            </span>
          ) : (
            '加载更多'
          )}
        </button>
      )}
      {done && blogs.length > 0 && <p className="muted" style={{ textAlign: 'center', marginTop: 16 }}>没有更多了</p>}
      {done && blogs.length === 0 && (
        <div className="card muted" style={{ textAlign: 'center', padding: 32 }}>
          暂无动态，先去关注一些用户吧。
        </div>
      )}
    </>
  );
}
