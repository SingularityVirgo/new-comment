import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { assetUrl, request } from '../api/request';
import type { Shop, Voucher } from '../api/types';
import { fenToYuan } from '../util/money';
import { useAuth } from '../auth/AuthContext';

export function ShopDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const nav = useNavigate();
  const [shop, setShop] = useState<Shop | null>(null);
  const [vouchers, setVouchers] = useState<Voucher[]>([]);
  const [err, setErr] = useState('');
  const [seckillMsg, setSeckillMsg] = useState<Record<number, string>>({});

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    void (async () => {
      const r = await request<Shop>(`/shop/${id}`);
      if (cancelled) return;
      if (!r.success) setErr(r.errorMsg || '店铺不存在');
      else {
        setErr('');
        setShop(r.data ?? null);
      }
      const v = await request<Voucher[]>(`/voucher/list/${id}`);
      if (cancelled) return;
      if (v.success) setVouchers((v.data as Voucher[]) || []);
    })();
    return () => {
      cancelled = true;
    };
  }, [id]);

  async function seckill(v: Voucher) {
    if (!user) {
      nav('/login', { state: { from: `/shop/${id}` } });
      return;
    }
    if (v.type !== 1) return;
    setSeckillMsg((m) => ({ ...m, [v.id]: '抢购中…' }));
    const r = await request<number>(`/voucher-order/seckill/${v.id}`, { method: 'POST' });
    if (r.success && r.data != null) {
      setSeckillMsg((m) => ({ ...m, [v.id]: `下单成功，订单号 ${r.data}` }));
      const v2 = await request<Voucher[]>(`/voucher/list/${id}`);
      if (v2.success) setVouchers((v2.data as Voucher[]) || []);
    } else {
      setSeckillMsg((m) => ({ ...m, [v.id]: r.errorMsg || '失败' }));
    }
  }

  function inSeckillWindow(v: Voucher): boolean {
    if (!v.beginTime || !v.endTime) return true;
    const now = Date.now();
    const b = new Date(v.beginTime).getTime();
    const e = new Date(v.endTime).getTime();
    return now >= b && now <= e;
  }

  if (err) return <div className="error-banner">{err}</div>;
  if (!shop) return <div className="muted">加载中…</div>;

  const imgs = shop.images
    ? shop.images
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean)
    : [];

  return (
    <div>
      <div className="card">
        <h1 style={{ marginTop: 0, fontSize: '1.35rem' }}>{shop.name}</h1>
        <div className="muted">
          {shop.area} · {shop.address}
        </div>
        <div style={{ marginTop: 8 }}>
          <span className="pill">评分 {(shop.score / 10).toFixed(1)}</span>{' '}
          <span className="pill">人均 ￥{shop.avgPrice ?? '—'}</span> <span className="pill">营业时间 {shop.openHours ?? '—'}</span>
        </div>
        {imgs.length > 0 && (
          <div className="img-grid" style={{ marginTop: 12 }}>
            {imgs.map((src) => (
              <img key={src} src={assetUrl(src)} alt="" />
            ))}
          </div>
        )}
      </div>

      <h2 style={{ fontSize: '1.1rem' }}>优惠券 · 秒杀</h2>
      {vouchers.length === 0 && <div className="muted card">暂无优惠券</div>}
      {vouchers.map((v) => (
        <div key={v.id} className="card">
          <div style={{ fontWeight: 600 }}>{v.title}</div>
          {v.subTitle && <div className="muted">{v.subTitle}</div>}
          <div style={{ marginTop: 8 }}>
            付 <b>￥{fenToYuan(v.payValue)}</b> 抵 <b>￥{fenToYuan(v.actualValue)}</b>
            {v.stock != null && <span className="muted"> · 库存 {v.stock}</span>}
          </div>
          {v.type === 1 && (
            <div style={{ marginTop: 8 }} className="muted">
              秒杀时段：{v.beginTime?.replace('T', ' ') ?? '—'} ~ {v.endTime?.replace('T', ' ') ?? '—'}
            </div>
          )}
          {v.rules && (
            <pre style={{ whiteSpace: 'pre-wrap', fontSize: '0.85rem', color: 'var(--muted)' }}>{v.rules}</pre>
          )}
          {v.type === 1 && (
            <button
              type="button"
              className="btn btn-primary"
              disabled={!inSeckillWindow(v) || (v.stock ?? 0) <= 0}
              onClick={() => void seckill(v)}
            >
              {!inSeckillWindow(v) ? '非秒杀时间' : (v.stock ?? 0) <= 0 ? '已抢完' : '立即秒杀'}
            </button>
          )}
          {v.type === 1 && seckillMsg[v.id] && <div className="muted" style={{ marginTop: 6 }}>{seckillMsg[v.id]}</div>}
          {v.type === 0 && <div className="muted" style={{ marginTop: 8 }}>普通券（后端未提供直购接口，可在管理端发放）</div>}
        </div>
      ))}

      <Link to="/shops" className="btn" style={{ display: 'inline-block', marginTop: 8 }}>
        返回商铺列表
      </Link>
    </div>
  );
}
