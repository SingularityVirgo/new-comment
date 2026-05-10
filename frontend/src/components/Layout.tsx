import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

function linkClass({ isActive }: { isActive: boolean }) {
  return isActive ? 'active' : undefined;
}

export function Layout() {
  const { user, logout } = useAuth();

  return (
    <div className="app-shell">
      <header className="top-nav">
        <NavLink to="/discover" className="brand" end={false}>
          <span className="brand-mark" aria-hidden>
            ✦
          </span>
          mzy-comment
        </NavLink>
        <NavLink to="/discover" className={linkClass}>
          探店
        </NavLink>
        <NavLink to="/shops" className={linkClass}>
          商铺
        </NavLink>
        {user && (
          <>
            <NavLink to="/publish" className={linkClass}>
              发笔记
            </NavLink>
            <NavLink to="/feed" className={linkClass}>
              关注
            </NavLink>
            <NavLink to="/profile" className={linkClass}>
              我的
            </NavLink>
          </>
        )}
        {!user && (
          <NavLink to="/login" className={linkClass}>
            登录
          </NavLink>
        )}
        {user && (
          <button type="button" className="btn btn-ghost" onClick={() => logout()}>
            退出
          </button>
        )}
      </header>
      <Outlet />
    </div>
  );
}
