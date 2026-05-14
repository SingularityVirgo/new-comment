import { useCallback, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { assetUrl, request } from '../api/request';
import type { Blog } from '../api/types';
import { EmptyState } from '../components/EmptyState';
import { LazyImage } from '../components/LazyImage';
import { PageSkeleton } from '../components/PageSkeleton';
import { useStaleResource } from '../hooks/useStaleResource';

function firstImage(images: string): string {
  const u = images?.split(',')?.[0]?.trim();
  return assetUrl(u);
}

export function Discover() {
  const nav = useNavigate();
  const [current, setCurrent] = useState(1);
  const key = useMemo(() => `blog-hot-${current}`, [current]);

  const fetcher = useCallback(async () => {
    const r = await request<Blog[]>('/blog/hot', { params: { current } });
    if (!r.success) throw new Error(r.errorMsg || '加载失败');
    return (r.data as Blog[]) || [];
  }, [current]);

  const { data: list = [], error, isLoading, isValidating, revalidate } = useStaleResource(key, fetcher, {
    staleMs: 5000,
    revalidateIntervalMs: 10_000,
  });

  return (
    <>
      <header className="page-head">
        <h1 className="page-title">热门探店</h1>
        <p className="page-lead">发现校园周边好店与真实笔记；列表采用 stale-while-revalidate，后台自动刷新。</p>
      </header>
      {error && <div className="error-banner">{error}</div>}
      {isValidating && list.length > 0 && (
        <div className="revalidate-hint" aria-live="polite">
          正在同步最新内容…
        </div>
      )}
      {isLoading && list.length === 0 && <PageSkeleton variant="list" />}
      {!isLoading &&
        list.map((b) => (
          <Link key={b.id} to={`/blog/${b.id}`} className="card route-fade-in" style={{ display: 'block', color: 'inherit' }}>
            <div className="row" style={{ alignItems: 'flex-start' }}>
              <LazyImage className="avatar" src={assetUrl(b.icon)} alt="" width={44} height={44} />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div className="feed-title">{b.title}</div>
                <div className="muted" style={{ marginTop: 4 }}>
                  {b.name} · 赞 {b.liked}
                </div>
              </div>
              {firstImage(b.images) && (
                <LazyImage className="feed-thumb" src={firstImage(b.images)} alt="" width={80} height={80} />
              )}
            </div>
          </Link>
        ))}
      {!isLoading && list.length === 0 && !error && (
        <EmptyState
          title="还没有热门笔记"
          description="换个时间再来看看，或先去商铺页逛逛。"
          actionLabel="去商铺"
          onAction={() => nav('/shops')}
        />
      )}
      {list.length >= 10 && (
        <div className="pager">
          <button type="button" className="btn" disabled={current <= 1} onClick={() => setCurrent((c) => Math.max(1, c - 1))}>
            上一页
          </button>
          <span className="muted">第 {current} 页</span>
          <button type="button" className="btn" onClick={() => setCurrent((c) => c + 1)}>
            下一页
          </button>
          <button type="button" className="btn btn-ghost" onClick={() => void revalidate()}>
            手动刷新
          </button>
        </div>
      )}
    </>
  );
}
