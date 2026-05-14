import { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { assetUrl, request, requestJson } from '../api/request';
import type { Blog, BlogComment, UserDTO } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { EmptyState } from '../components/EmptyState';
import { LazyImage } from '../components/LazyImage';
import { PageSkeleton } from '../components/PageSkeleton';
import { useToast } from '../components/Toast';
import { useDebouncedSubmit } from '../hooks/useDebouncedSubmit';

export function BlogDetail() {
  const { id } = useParams();
  const nav = useNavigate();
  const { user } = useAuth();
  const { showToast } = useToast();
  const { locked: commentLocked, run: runCommentSubmit } = useDebouncedSubmit();
  const deleteTimers = useRef<Map<number, ReturnType<typeof setTimeout>>>(new Map());

  const [blog, setBlog] = useState<Blog | null>(null);
  const [err, setErr] = useState('');
  const [loading, setLoading] = useState(true);
  const [likers, setLikers] = useState<UserDTO[] | null>(null);
  const [comments, setComments] = useState<BlogComment[]>([]);
  const [commentMsg, setCommentMsg] = useState('');
  const [newComment, setNewComment] = useState('');

  const loadComments = useCallback(async (blogId: string) => {
    const r = await request<BlogComment[]>(`/blog-comments/of-blog/${blogId}`);
    if (r.success && Array.isArray(r.data)) setComments(r.data);
  }, []);

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    setLoading(true);
    setErr('');
    setBlog(null);
    void (async () => {
      const r = await request<Blog>(`/blog/${id}`);
      if (cancelled) return;
      setLoading(false);
      if (!r.success) setErr(r.errorMsg || '加载失败');
      else {
        setErr('');
        setBlog(r.data ?? null);
        void loadComments(id);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [id, loadComments]);

  useEffect(() => {
    return () => {
      deleteTimers.current.forEach((t) => clearTimeout(t));
      deleteTimers.current.clear();
    };
  }, []);

  async function toggleLike() {
    if (!user) {
      nav('/login', { state: { from: `/blog/${id}` } });
      return;
    }
    if (!blog || !id) return;
    const snapshot = blog;
    setBlog({
      ...blog,
      isLike: !blog.isLike,
      liked: blog.isLike ? Math.max(0, blog.liked - 1) : blog.liked + 1,
    });
    const r = await request(`/blog/like/${id}`, { method: 'PUT' });
    if (!r.success) {
      setBlog(snapshot);
      showToast(r.errorMsg || '操作失败，已恢复点赞状态');
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

  async function submitComment() {
    if (!user || !id) {
      nav('/login', { state: { from: `/blog/${id}` } });
      return;
    }
    const text = newComment.trim();
    if (!text) return;
    try {
      await runCommentSubmit(async () => {
        setCommentMsg('');
        const r = await requestJson<number>('/blog-comments', {
          blogId: Number(id),
          content: text,
          parentId: 0,
          answerId: 0,
        });
        if (!r.success) {
          setCommentMsg(r.errorMsg || '评论失败');
          return;
        }
        setNewComment('');
        const detail = await request<Blog>(`/blog/${id}`);
        if (detail.success && detail.data) setBlog(detail.data);
        await loadComments(id);
      });
    } catch {
      /* 防抖窗口内重复提交 */
    }
  }

  function queueDeleteComment(c: BlogComment) {
    if (!id) return;
    setComments((prev) => prev.filter((x) => x.id !== c.id));
    showToast({
      message: '评论已移除',
      undoSeconds: 3,
      undo: () => {
        const t = deleteTimers.current.get(c.id);
        if (t) clearTimeout(t);
        deleteTimers.current.delete(c.id);
        setComments((prev) => [...prev, c].sort((a, b) => a.id - b.id));
      },
    });
    const t = window.setTimeout(() => {
      deleteTimers.current.delete(c.id);
      void (async () => {
        const r = await request(`/blog-comments/${c.id}`, { method: 'DELETE' });
        if (!r.success) {
          showToast(r.errorMsg || '删除失败，已为你刷新评论');
          setComments((prev) => [...prev, c].sort((a, b) => a.id - b.id));
          return;
        }
        const detail = await request<Blog>(`/blog/${id}`);
        if (detail.success && detail.data) setBlog(detail.data);
        await loadComments(id);
      })();
    }, 3000);
    deleteTimers.current.set(c.id, t);
  }

  if (err) return <div className="error-banner">{err}</div>;
  if (loading && !blog)
    return (
      <>
        <PageSkeleton variant="article" />
        <span className="visually-hidden">加载笔记中</span>
      </>
    );
  if (!blog) return null;

  const imgs = blog.images
    ? blog.images
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean)
    : [];

  return (
    <article className="card route-fade-in" style={{ padding: 0, overflow: 'hidden' }}>
      <div style={{ padding: '22px 22px 0' }}>
        <div className="row" style={{ marginBottom: 16 }}>
          <Link to={`/user/${blog.userId}`}>
            <LazyImage className="avatar" src={assetUrl(blog.icon)} alt="" width={48} height={48} />
          </Link>
          <div>
            <Link to={`/user/${blog.userId}`} style={{ fontWeight: 600, color: 'var(--text)' }}>
              {blog.name}
            </Link>
            <div className="muted">笔记 · 关联店铺 #{blog.shopId}</div>
          </div>
        </div>
        <h1 className="page-title" style={{ fontSize: 'clamp(1.35rem, 3.5vw, 1.75rem)', WebkitTextFillColor: 'unset', color: 'var(--text)' }}>
          {blog.title}
        </h1>
      </div>
      {imgs.length > 0 && (
        <div className="img-grid" style={{ margin: '0 16px 16px', padding: 0 }}>
          {imgs.map((src) => (
            <LazyImage key={src} src={assetUrl(src)} alt="" />
          ))}
        </div>
      )}
      <div style={{ padding: '0 22px 22px' }}>
        <div className="blog-body" dangerouslySetInnerHTML={{ __html: blog.content }} />
        <div className="row" style={{ marginTop: 22, flexWrap: 'wrap', gap: 10 }}>
          <button type="button" className="btn btn-primary" onClick={() => void toggleLike()} aria-pressed={blog.isLike}>
            {blog.isLike ? '取消赞' : '点赞'} · {blog.liked}
          </button>
          <button type="button" className="btn" onClick={() => void showLikers()}>
            点赞排行
          </button>
          <Link to={`/shop/${blog.shopId}`} className="btn">
            查看店铺
          </Link>
        </div>

        {likers && (
          <div style={{ marginTop: 22, borderTop: '1px solid var(--stroke)', paddingTop: 18 }}>
            <div className="section-title" style={{ marginTop: 0 }}>
              点赞用户（Top5）
            </div>
            <div className="row" style={{ flexWrap: 'wrap', gap: 10 }}>
              {likers.length === 0 && <span className="muted">暂无</span>}
              {likers.map((u) => (
                <Link key={u.id} to={`/user/${u.id}`} className="row" style={{ gap: 8 }}>
                  <LazyImage className="avatar" src={assetUrl(u.icon)} alt="" width={32} height={32} />
                  <span style={{ color: 'var(--text)' }}>{u.nickName}</span>
                </Link>
              ))}
            </div>
            <button type="button" className="btn btn-ghost" style={{ marginTop: 12 }} onClick={() => setLikers(null)}>
              关闭
            </button>
          </div>
        )}

        <div style={{ marginTop: 28, borderTop: '1px solid var(--stroke)', paddingTop: 20 }}>
          <div className="section-title" style={{ marginTop: 0 }}>
            评论 · {blog.comments ?? comments.length}
          </div>
          {commentMsg && <div className="error-banner" style={{ marginBottom: 10 }}>{commentMsg}</div>}
          <div className="row" style={{ alignItems: 'flex-start', gap: 12, flexWrap: 'wrap' }}>
            <textarea
              className="input"
              style={{ flex: 1, minWidth: 200, minHeight: 72, resize: 'vertical' }}
              placeholder={user ? '写一条评论…（Ctrl+Enter 发送）' : '登录后发表评论'}
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              disabled={!user}
              aria-label="评论正文"
              onKeyDown={(e) => {
                if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
                  e.preventDefault();
                  void submitComment();
                }
              }}
            />
            <button
              type="button"
              className="btn btn-primary"
              disabled={!user || commentLocked}
              onClick={() => void submitComment()}
            >
              {commentLocked ? '请稍候…' : '发送'}
            </button>
          </div>
          {comments.length === 0 ? (
            <div style={{ marginTop: 18 }}>
              <EmptyState
                title="还没有评论"
                description="抢沙发，发表第一条看法吧。"
                actionLabel={user ? undefined : '去登录'}
                onAction={user ? undefined : () => nav('/login', { state: { from: `/blog/${id}` } })}
              />
            </div>
          ) : (
            <ul style={{ listStyle: 'none', padding: 0, margin: '18px 0 0' }}>
              {comments.map((c) => (
                <li key={c.id} style={{ padding: '12px 0', borderBottom: '1px solid var(--stroke)' }}>
                  <div className="row" style={{ gap: 10, alignItems: 'center', marginBottom: 6 }}>
                    <LazyImage className="avatar" src={assetUrl(c.icon)} alt="" width={36} height={36} />
                    <span style={{ fontWeight: 600 }}>{c.name || `用户${c.userId}`}</span>
                    {user && user.id === c.userId && (
                      <button
                        type="button"
                        className="btn btn-ghost"
                        style={{ marginLeft: 'auto' }}
                        onClick={() => queueDeleteComment(c)}
                      >
                        删除
                      </button>
                    )}
                  </div>
                  <div style={{ whiteSpace: 'pre-wrap' }}>{c.content}</div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </article>
  );
}
