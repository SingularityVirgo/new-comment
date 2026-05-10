import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { assetUrl, request } from '../api/request';
import type { Blog, UserDTO } from '../api/types';
import { useAuth } from '../auth/AuthContext';

export function BlogDetail() {
  const { id } = useParams();
  const nav = useNavigate();
  const { user } = useAuth();
  const [blog, setBlog] = useState<Blog | null>(null);
  const [err, setErr] = useState('');
  const [likers, setLikers] = useState<UserDTO[] | null>(null);

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    (async () => {
      const r = await request<Blog>(`/blog/${id}`);
      if (cancelled) return;
      if (!r.success) setErr(r.errorMsg || '加载失败');
      else {
        setErr('');
        setBlog(r.data ?? null);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [id]);

  async function toggleLike() {
    if (!user) {
      nav('/login', { state: { from: `/blog/${id}` } });
      return;
    }
    const r = await request(`/blog/like/${id}`, { method: 'PUT' });
    if (!r.success) {
      alert(r.errorMsg || '操作失败');
      return;
    }
    const detail = await request<Blog>(`/blog/${id}`);
    if (detail.success && detail.data) setBlog(detail.data);
  }

  async function showLikers() {
    if (!id) return;
    const r = await request<UserDTO[]>(`/blog/likes/${id}`);
    if (r.success) setLikers((r.data as UserDTO[]) || []);
  }

  if (err) return <div className="error-banner">{err}</div>;
  if (!blog) return <div className="muted">加载中…</div>;

  const imgs = blog.images
    ? blog.images
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean)
    : [];

  return (
    <article className="card">
      <div className="row" style={{ marginBottom: 12 }}>
        <Link to={`/user/${blog.userId}`}>
          <img className="avatar" src={assetUrl(blog.icon)} alt="" width={48} height={48} />
        </Link>
        <div>
          <Link to={`/user/${blog.userId}`} style={{ fontWeight: 600, color: 'inherit' }}>
            {blog.name}
          </Link>
          <div className="muted">笔记 · 关联店铺 #{blog.shopId}</div>
        </div>
      </div>
      <h1 style={{ fontSize: '1.35rem', margin: '0 0 12px' }}>{blog.title}</h1>
      {imgs.length > 0 && (
        <div className="img-grid" style={{ marginBottom: 12 }}>
          {imgs.map((src) => (
            <img key={src} src={assetUrl(src)} alt="" />
          ))}
        </div>
      )}
      <div
        className="blog-body"
        style={{ lineHeight: 1.65 }}
        dangerouslySetInnerHTML={{ __html: blog.content }}
      />
      <div className="row" style={{ marginTop: 16, flexWrap: 'wrap', gap: 8 }}>
        <button type="button" className="btn btn-primary" onClick={() => void toggleLike()}>
          {blog.isLike ? '取消赞' : '点赞'}（{blog.liked}）
        </button>
        <button type="button" className="btn" onClick={() => void showLikers()}>
          点赞排行
        </button>
        <Link to={`/shop/${blog.shopId}`} className="btn">
          查看店铺
        </Link>
      </div>

      {likers && (
        <div style={{ marginTop: 16, borderTop: '1px solid var(--border)', paddingTop: 12 }}>
          <div style={{ fontWeight: 600, marginBottom: 8 }}>点赞用户（Top5）</div>
          <div className="row" style={{ flexWrap: 'wrap' }}>
            {likers.length === 0 && <span className="muted">暂无</span>}
            {likers.map((u) => (
              <Link key={u.id} to={`/user/${u.id}`} className="row" style={{ gap: 6 }}>
                <img className="avatar" src={assetUrl(u.icon)} alt="" width={32} height={32} />
                <span>{u.nickName}</span>
              </Link>
            ))}
          </div>
          <button type="button" className="btn btn-ghost" style={{ marginTop: 8 }} onClick={() => setLikers(null)}>
            关闭
          </button>
        </div>
      )}
    </article>
  );
}
