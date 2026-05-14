import { Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { PrefetchNavLink } from './PrefetchNavLink';
import { useOnlineStatus } from '../hooks/useOnlineStatus';

function linkClass({ isActive }: { isActive: boolean }) {
  return isActive ? 'active' : undefined;
}

export function Layout() {
  const { user, logout } = useAuth();
  const online = useOnlineStatus();

  return (
    <div className="app-shell">
      <a href="#main-content" className="skip-link">
        跳到正文
      </a>
      {!online && (
        <div className="offline-banner" role="status">
          当前处于离线或网络不可用，页面将尽量展示已缓存的数据；联网后自动恢复。
        </div>
      )}
      <header className="top-nav" role="navigation" aria-label="主导航">
        <PrefetchNavLink to="/discover" className="brand" end={false}>
          <span className="brand-mark" aria-hidden>
            ✦
          </span>
          mzy-comment
        </PrefetchNavLink>
        <PrefetchNavLink to="/discover" className={linkClass}>
          探店
        </PrefetchNavLink>
        <PrefetchNavLink to="/shops" className={linkClass}>
          商铺
        </PrefetchNavLink>
        {user && (
          <>
            <PrefetchNavLink to="/publish" className={linkClass}>
              发笔记
            </PrefetchNavLink>
            <PrefetchNavLink to="/feed" className={linkClass}>
              关注
            </PrefetchNavLink>
            <PrefetchNavLink to="/profile" className={linkClass}>
              我的
            </PrefetchNavLink>
          </>
        )}
        {!user && (
          <PrefetchNavLink to="/login" className={linkClass}>
            登录
          </PrefetchNavLink>
        )}
        {user && (
          <button type="button" className="btn btn-ghost" onClick={() => logout()}>
            退出
          </button>
        )}
      </header>
      <main id="main-content" tabIndex={-1}>
        <Outlet />
      </main>
    </div>
  );
}
