export function apiBase(): string {
  if (import.meta.env.DEV) return '/api';
  return import.meta.env.VITE_API_BASE || 'http://localhost:8081';
}

/** 静态资源（店铺图、笔记图等），默认空则使用相对路径 */
export function staticBase(): string {
  return import.meta.env.VITE_STATIC_BASE || '';
}

export function assetUrl(path: string | undefined | null): string {
  if (!path) return '';
  const p = path.trim();
  if (p.startsWith('http://') || p.startsWith('https://')) return p;
  const base = staticBase().replace(/\/$/, '');
  if (p.startsWith('/')) return `${base}${p}`;
  return `${base}/${p}`;
}

export interface ApiResult<T = unknown> {
  success: boolean;
  errorMsg?: string | null;
  data?: T;
  total?: number | null;
}

function buildUrl(path: string, params?: Record<string, string | number | undefined | null>): string {
  let url = apiBase() + path;
  if (!params) return url;
  const sp = new URLSearchParams();
  for (const [k, v] of Object.entries(params)) {
    if (v !== undefined && v !== null) sp.set(k, String(v));
  }
  const q = sp.toString();
  if (q) url += (url.includes('?') ? '&' : '?') + q;
  return url;
}

/** GET 并发去重：相同 URL 在飞行中合并为单次请求 */
const inflightGet = new Map<string, Promise<ApiResult<unknown>>>();

async function doFetch<T>(url: string, init: RequestInit): Promise<ApiResult<T>> {
  const token = localStorage.getItem('token');
  const res = await fetch(url, {
    ...init,
    headers: {
      Accept: 'application/json',
      ...(init.headers as Record<string, string>),
      ...(token ? { authorization: token } : {}),
    },
  });

  const text = await res.text();
  let json: ApiResult<T> = { success: false, errorMsg: '响应解析失败' };
  try {
    json = text ? (JSON.parse(text) as ApiResult<T>) : json;
  } catch {
    json = { success: false, errorMsg: text || `HTTP ${res.status}` };
  }

  if (res.status === 401) {
    localStorage.removeItem('token');
    window.dispatchEvent(new Event('auth-change'));
  }

  return json;
}

export async function request<T>(
  path: string,
  init: RequestInit & { params?: Record<string, string | number | undefined | null> } = {},
): Promise<ApiResult<T>> {
  const { params, headers, ...rest } = init;
  const url = buildUrl(path, params);
  const method = (rest.method || 'GET').toUpperCase();

  if (method === 'GET') {
    const existing = inflightGet.get(url);
    if (existing) return existing as Promise<ApiResult<T>>;
    const p = doFetch<T>(url, { ...rest, headers }).finally(() => {
      if (inflightGet.get(url) === p) inflightGet.delete(url);
    });
    inflightGet.set(url, p as Promise<ApiResult<unknown>>);
    return p;
  }

  return doFetch<T>(url, { ...rest, headers });
}

export async function requestJson<T>(path: string, body: unknown, method = 'POST'): Promise<ApiResult<T>> {
  return request<T>(path, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
}
