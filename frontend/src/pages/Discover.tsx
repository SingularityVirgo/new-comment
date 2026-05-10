import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { assetUrl, request } from '../api/request';
import type { Blog } from '../api/types';

function firstImage(images: string): string {
  const u = images?.split(',')?.[0]?.trim();
  return assetUrl(u);
}

export function Discover() {
  const [list, setList] = useState<Blog[]>([]);
  const [current, setCurrent] = useState(1);
  const [err, setErr] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoading(true);
      const r = await request<Blog[]>('/blog/hot', { params: { current } });
      if (cancelled) return;
      setLoading(false);
      if (!r.success) setErr(r.errorMsg || '加载失败');
      else {
        setErr('');
        setList((r.data as Blog[]) || []);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [current]);

  return (
    <>
      <h1 style={{ fontSize: '1.25rem', margin: '0 0 12px' }}>热门探店</h1>
      {err && <div className="error-banner">{err}</div>}
      {loading && <div className="muted">加载中…</div>}
      {!loading &&
        list.map((b) => (
          <Link key={b.id} to={`/blog/${b.id}`} className="card" style={{ display: 'block', color: 'inherit' }}>
            <div className="row" style={{ alignItems: 'flex-start' }}>
              <img className="avatar" src={assetUrl(b.icon)} alt="" width={40} height={40} />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontWeight: 600 }}>{b.title}</div>
                <div className="muted">
                  {b.name} · 赞 {b.liked}
                </div>
              </div>
              {firstImage(b.images) && (
                <img src={firstImage(b.images)} alt="" style={{ width: 72, height: 72, borderRadius: 8, objectFit: 'cover' }} />
              )}
            </div>
          </Link>
        ))}
      {!loading && list.length === 0 && <div className="muted">暂无数据</div>}
      {list.length >= 10 && (
        <div className="row" style={{ justifyContent: 'center', marginTop: 8 }}>
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
