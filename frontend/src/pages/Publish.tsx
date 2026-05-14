import { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { apiBase, request, requestJson } from '../api/request';
import type { Blog, Shop } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { useDebouncedSubmit } from '../hooks/useDebouncedSubmit';

const PUBLISH_DRAFT_KEY = 'mzy-publish-draft-v1';

export function Publish() {
  const { user } = useAuth();
  const nav = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const paramShopId = searchParams.get('shopId');

  const [shopName, setShopName] = useState('');
  const [shopLoadErr, setShopLoadErr] = useState('');
  const [shopLoading, setShopLoading] = useState(false);

  const [nameQuery, setNameQuery] = useState('');
  const [searchHits, setSearchHits] = useState<Shop[]>([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchOpen, setSearchOpen] = useState(false);

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [images, setImages] = useState<string[]>([]);
  const [uploading, setUploading] = useState(false);
  const [msg, setMsg] = useState('');
  const { locked: submitLocked, run: runSubmit } = useDebouncedSubmit();

  const searchDebounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const draftLoaded = useRef(false);

  useEffect(() => {
    if (draftLoaded.current) return;
    draftLoaded.current = true;
    try {
      const raw = localStorage.getItem(PUBLISH_DRAFT_KEY);
      if (!raw) return;
      const d = JSON.parse(raw) as { title?: string; content?: string; images?: string[]; shopId?: string };
      if (typeof d.title === 'string') setTitle(d.title);
      if (typeof d.content === 'string') setContent(d.content);
      if (Array.isArray(d.images)) setImages(d.images);
      const sidNow = new URLSearchParams(window.location.search).get('shopId');
      if (d.shopId && !sidNow) {
        setSearchParams(
          (prev) => {
            const next = new URLSearchParams(prev);
            next.set('shopId', d.shopId!);
            return next;
          },
          { replace: true },
        );
      }
    } catch {
      /* ignore */
    }
    // 仅挂载时恢复草稿，避免编辑过程中 URL 变化误触发
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    const t = window.setTimeout(() => {
      try {
        localStorage.setItem(
          PUBLISH_DRAFT_KEY,
          JSON.stringify({ title, content, images, shopId: paramShopId || null }),
        );
      } catch {
        /* quota */
      }
    }, 500);
    return () => window.clearTimeout(t);
  }, [title, content, images, paramShopId]);

  useEffect(() => {
    if (!paramShopId) {
      setShopName('');
      setShopLoadErr('');
      setShopLoading(false);
      return;
    }
    const sid = Number(paramShopId);
    if (!Number.isFinite(sid) || sid <= 0) {
      setShopName('');
      setShopLoadErr('链接中的店铺 ID 无效');
      setShopLoading(false);
      return;
    }
    let cancelled = false;
    setShopLoading(true);
    setShopLoadErr('');
    void (async () => {
      const r = await request<Shop>(`/shop/${sid}`);
      if (cancelled) return;
      setShopLoading(false);
      if (r.success && r.data) {
        setShopName(r.data.name);
        setShopLoadErr('');
      } else {
        setShopName('');
        setShopLoadErr(r.errorMsg || '未找到该店铺');
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [paramShopId]);

  const pickShop = useCallback(
    (shop: Shop) => {
      setSearchParams(
        (prev) => {
          const next = new URLSearchParams(prev);
          next.set('shopId', String(shop.id));
          return next;
        },
        { replace: true },
      );
      setShopName(shop.name);
      setShopLoadErr('');
      setNameQuery('');
      setSearchHits([]);
      setSearchOpen(false);
    },
    [setSearchParams],
  );

  useEffect(() => {
    if (searchDebounceRef.current) clearTimeout(searchDebounceRef.current);
    const q = nameQuery.trim();
    if (q.length < 1) {
      setSearchHits([]);
      setSearchLoading(false);
      return;
    }
    setSearchLoading(true);
    searchDebounceRef.current = setTimeout(() => {
      void (async () => {
        const r = await request<Shop[]>('/shop/of/name', { params: { name: q, current: 1 } });
        setSearchLoading(false);
        if (!r.success) {
          setSearchHits([]);
          return;
        }
        setSearchHits((r.data as Shop[]) || []);
      })();
    }, 300);
    return () => {
      if (searchDebounceRef.current) clearTimeout(searchDebounceRef.current);
    };
  }, [nameQuery]);

  function clearShop() {
    setSearchParams(
      (prev) => {
        const next = new URLSearchParams(prev);
        next.delete('shopId');
        return next;
      },
      { replace: true },
    );
    setShopName('');
    setShopLoadErr('');
    setNameQuery('');
    setSearchHits([]);
    setSearchOpen(false);
  }

  async function onUpload(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file) return;
    setUploading(true);
    setMsg('');
    const fd = new FormData();
    fd.append('file', file);
    const token = localStorage.getItem('token');
    const res = await fetch(`${apiBase()}/upload/blog`, {
      method: 'POST',
      headers: token ? { authorization: token } : {},
      body: fd,
    });
    const json = (await res.json()) as { success: boolean; data?: string; errorMsg?: string };
    setUploading(false);
    if (!json.success || !json.data) setMsg(json.errorMsg || '上传失败');
    else setImages((prev) => [...prev, json.data as string]);
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setMsg('');
    const sid = paramShopId ? Number(paramShopId) : NaN;
    if (!Number.isFinite(sid) || sid <= 0) {
      setMsg('请先选择关联店铺（搜索店名或从商铺页进入）');
      return;
    }
    try {
      await runSubmit(async () => {
        const blog: Partial<Blog> = {
          shopId: sid,
          title,
          content,
          images: images.join(','),
        };
        const r = await requestJson<number>('/blog', blog);
        if (!r.success) {
          setMsg(r.errorMsg || '发布失败');
          return;
        }
        try {
          localStorage.removeItem(PUBLISH_DRAFT_KEY);
        } catch {
          /* ignore */
        }
        nav(`/blog/${r.data}`);
      });
    } catch {
      /* 500ms 内防重复提交 */
    }
  }

  if (!user) return null;

  const hasShop = paramShopId && Number(paramShopId) > 0 && Number.isFinite(Number(paramShopId));

  return (
    <>
      <header className="page-head">
        <h1 className="page-title">发布探店</h1>
        <p className="page-lead">选好店铺、写好标题与正文，配图上传后由后端写入相对路径。</p>
      </header>
      <div className="card">
        <h2 style={{ marginTop: 0, fontSize: '1.1rem' }}>笔记表单</h2>
        <p className="muted" style={{ marginTop: 0, marginBottom: 12 }}>
          标题与正文会每 0.5s 自动写入浏览器本地草稿（localStorage），意外关闭可恢复。
        </p>
        {msg && <div className="error-banner">{msg}</div>}
        <form onSubmit={(e) => void submit(e)}>
          <div className="field">
            <label>关联店铺</label>
            {hasShop && (
              <div
                className="card"
                style={{
                  marginBottom: 10,
                  padding: '12px 14px',
                  background: 'var(--surface-2, rgba(0,0,0,0.04))',
                  border: '1px solid var(--border, rgba(0,0,0,0.08))',
                }}
              >
                <div style={{ fontWeight: 600 }}>
                  {shopLoading && !shopName ? '加载店铺信息…' : shopName || '—'}
                </div>
                <div className="muted" style={{ marginTop: 6, fontSize: '0.9rem' }}>
                  店铺 ID {paramShopId}
                  {shopLoadErr && <span style={{ color: 'var(--danger, #c00)', marginLeft: 8 }}>{shopLoadErr}</span>}
                </div>
                <button type="button" className="btn btn-ghost" style={{ marginTop: 10 }} onClick={() => clearShop()}>
                  更换店铺
                </button>
              </div>
            )}
            <input
              className="input"
              value={nameQuery}
              onChange={(e) => {
                setNameQuery(e.target.value);
                setSearchOpen(true);
              }}
              onFocus={() => setSearchOpen(true)}
              onBlur={() => {
                window.setTimeout(() => setSearchOpen(false), 180);
              }}
              placeholder={hasShop ? '搜索其他店名以切换…' : '输入店名搜索并选择'}
              autoComplete="off"
            />
            {searchOpen && nameQuery.trim().length >= 1 && (
              <div
                className="card"
                style={{
                  marginTop: 8,
                  maxHeight: 240,
                  overflowY: 'auto',
                  padding: 0,
                  border: '1px solid var(--border, rgba(0,0,0,0.08))',
                }}
              >
                {searchLoading && (
                  <div className="muted" style={{ padding: 12 }}>
                    搜索中…
                  </div>
                )}
                {!searchLoading && searchHits.length === 0 && (
                  <div className="muted" style={{ padding: 12 }}>
                    无匹配店铺，换个关键词试试
                  </div>
                )}
                {!searchLoading &&
                  searchHits.map((s) => (
                    <button
                      key={s.id}
                      type="button"
                      onMouseDown={(e) => e.preventDefault()}
                      onClick={() => pickShop(s)}
                      style={{
                        display: 'block',
                        width: '100%',
                        textAlign: 'left',
                        padding: '10px 14px',
                        border: 'none',
                        borderBottom: '1px solid var(--border, rgba(0,0,0,0.06))',
                        background: 'transparent',
                        cursor: 'pointer',
                        color: 'inherit',
                        font: 'inherit',
                      }}
                    >
                      <div style={{ fontWeight: 600 }}>{s.name}</div>
                      <div className="muted" style={{ fontSize: '0.85rem', marginTop: 2 }}>
                        {s.area} · ID {s.id}
                      </div>
                    </button>
                  ))}
              </div>
            )}
            <p className="muted" style={{ marginTop: 8, marginBottom: 0 }}>
              也可在{' '}
              <Link to="/shops">商铺列表</Link> 进入店铺页，点击「在此店发笔记」自动带上店铺。
            </p>
          </div>
          <div className="field">
            <label>标题</label>
            <input className="input" value={title} onChange={(e) => setTitle(e.target.value)} required />
          </div>
          <div className="field">
            <label>正文（支持 HTML）</label>
            <textarea className="input" value={content} onChange={(e) => setContent(e.target.value)} required />
          </div>
          <div className="field">
            <label>图片</label>
            <input type="file" accept="image/*" onChange={(e) => void onUpload(e)} disabled={uploading} style={{ color: 'var(--text-soft)' }} />
            <p className="muted">上传到后端（开启 OSS 时返回图片完整 URL，否则为本地相对路径），写入笔记后在详情页直接展示。</p>
            {uploading && (
              <div className="row" style={{ marginTop: 8 }}>
                <span className="spinner" aria-hidden />
                <span className="muted">上传中…</span>
              </div>
            )}
            {images.length > 0 && (
              <ul className="muted" style={{ fontSize: '0.85rem', marginTop: 8 }}>
                {images.map((p) => (
                  <li key={p}>{p}</li>
                ))}
              </ul>
            )}
          </div>
          <button type="submit" className="btn btn-primary" disabled={submitLocked}>
            {submitLocked ? '提交中…' : '发布笔记'}
          </button>
        </form>
      </div>
    </>
  );
}
