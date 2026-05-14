import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { assetUrl, request } from '../api/request';
import type { Shop, ShopType } from '../api/types';
import { EmptyState } from '../components/EmptyState';
import { LazyImage } from '../components/LazyImage';
import { PageSkeleton } from '../components/PageSkeleton';

export function Shops() {
  const [types, setTypes] = useState<ShopType[]>([]);
  const [typeId, setTypeId] = useState<number | null>(null);
  const [shops, setShops] = useState<Shop[]>([]);
  const [current, setCurrent] = useState(1);
  const [name, setName] = useState('');
  const [mode, setMode] = useState<'type' | 'search'>('type');
  const [err, setErr] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    void (async () => {
      const r = await request<ShopType[]>('/shop-type/list');
      if (r.success && r.data?.length) {
        setTypes(r.data);
        setTypeId((prev) => prev ?? r.data![0].id);
      }
    })();
  }, []);

  useEffect(() => {
    if (mode === 'search') return;
    if (typeId == null) return;
    let cancelled = false;
    setLoading(true);
    void (async () => {
      const r = await request<Shop[]>('/shop/of/type', { params: { typeId, current } });
      if (cancelled) return;
      setLoading(false);
      if (!r.success) setErr(r.errorMsg || '加载失败');
      else {
        setErr('');
        setShops((r.data as Shop[]) || []);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [typeId, current, mode]);

  useEffect(() => {
    if (mode !== 'search') return;
    let cancelled = false;
    setLoading(true);
    const t = setTimeout(() => {
      void (async () => {
        const r = await request<Shop[]>('/shop/of/name', { params: { name: name || undefined, current } });
        if (cancelled) return;
        setLoading(false);
        if (!r.success) setErr(r.errorMsg || '搜索失败');
        else {
          setErr('');
          setShops((r.data as Shop[]) || []);
        }
      })();
    }, 300);
    return () => {
      cancelled = true;
      clearTimeout(t);
    };
  }, [name, current, mode]);

  const cover = (s: Shop) => {
    const u = s.images?.split(',')?.[0]?.trim();
    return assetUrl(u);
  };

  return (
    <>
      <header className="page-head">
        <h1 className="page-title">商铺</h1>
        <p className="page-lead">按分类浏览或搜索店名；首屏骨架占位，图片懒加载与模糊过渡。</p>
      </header>
      <div className="card">
        <div className="row" style={{ marginBottom: 14, flexWrap: 'wrap' }}>
          <button type="button" className={mode === 'type' ? 'btn btn-primary' : 'btn'} onClick={() => setMode('type')}>
            按分类
          </button>
          <button type="button" className={mode === 'search' ? 'btn btn-primary' : 'btn'} onClick={() => setMode('search')}>
            按名称
          </button>
        </div>
        {mode === 'type' && (
          <div className="grid-2">
            {types.map((t) => (
              <button
                key={t.id}
                type="button"
                className={typeId === t.id ? 'btn btn-primary' : 'btn'}
                onClick={() => {
                  setTypeId(t.id);
                  setCurrent(1);
                }}
              >
                {t.name}
              </button>
            ))}
          </div>
        )}
        {mode === 'search' && (
          <div className="field" style={{ marginBottom: 0 }}>
            <label htmlFor="shop-search">关键字</label>
            <input
              id="shop-search"
              className="input"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="商铺名称"
            />
          </div>
        )}
      </div>
      {err && <div className="error-banner">{err}</div>}
      {loading && <PageSkeleton variant="list" />}
      {!loading &&
        shops.map((s) => (
          <Link key={s.id} to={`/shop/${s.id}`} className="card route-fade-in" style={{ display: 'flex', gap: 14, color: 'inherit', alignItems: 'center' }}>
            {cover(s) && <LazyImage className="feed-thumb" src={cover(s)} alt="" width={88} height={88} style={{ width: 88, height: 88 }} />}
            <div style={{ flex: 1, minWidth: 0 }}>
              <div className="feed-title">{s.name}</div>
              <div className="muted" style={{ marginTop: 4 }}>
                {s.area}
              </div>
              <div className="muted" style={{ fontSize: '0.8rem', marginTop: 6 }}>
                评分 {(s.score / 10).toFixed(1)} · ￥{s.avgPrice ?? '—'} · 销量 {s.sold}
              </div>
            </div>
          </Link>
        ))}
      {!loading && shops.length === 0 && !err && (
        <EmptyState
          title="没有找到商铺"
          description="试试切换分类、清空搜索词，或稍后再试。"
          actionLabel="切到按分类"
          onAction={() => setMode('type')}
        />
      )}
      {shops.length >= 10 && (
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
