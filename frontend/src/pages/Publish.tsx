import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiBase, requestJson } from '../api/request';
import type { Blog } from '../api/types';
import { useAuth } from '../auth/AuthContext';

export function Publish() {
  const { user } = useAuth();
  const nav = useNavigate();
  const [shopId, setShopId] = useState('');
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [images, setImages] = useState<string[]>([]);
  const [uploading, setUploading] = useState(false);
  const [msg, setMsg] = useState('');
  const [submitting, setSubmitting] = useState(false);

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
    const sid = Number(shopId);
    if (!Number.isFinite(sid) || sid <= 0) {
      setMsg('请填写有效的店铺 ID');
      return;
    }
    setSubmitting(true);
    const blog: Partial<Blog> = {
      shopId: sid,
      title,
      content,
      images: images.join(','),
    };
    const r = await requestJson<number>('/blog', blog);
    setSubmitting(false);
    if (!r.success) setMsg(r.errorMsg || '发布失败');
    else nav(`/blog/${r.data}`);
  }

  if (!user) return null;

  return (
    <>
      <header className="page-head">
        <h1 className="page-title">发布探店</h1>
        <p className="page-lead">写好标题与正文，配图上传后由后端写入相对路径。</p>
      </header>
      <div className="card">
        <h2 style={{ marginTop: 0, fontSize: '1.1rem' }}>笔记表单</h2>
        {msg && <div className="error-banner">{msg}</div>}
        <form onSubmit={(e) => void submit(e)}>
          <div className="field">
            <label>店铺 ID</label>
            <input className="input" value={shopId} onChange={(e) => setShopId(e.target.value)} placeholder="数字，可在商铺页查看" />
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
            <p className="muted">上传到后端本地目录，返回相对路径后写入笔记。</p>
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
          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? '提交中…' : '发布笔记'}
          </button>
        </form>
      </div>
    </>
  );
}
