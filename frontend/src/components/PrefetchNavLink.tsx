import { useCallback } from 'react';
import { NavLink, type NavLinkProps } from 'react-router-dom';
import { prefetchRoute } from '../routes/prefetch';

/**
 * Hover / 聚焦时 prefetch 路由 chunk（Code Splitting + 预加载策略）。
 */
export function PrefetchNavLink({ onMouseEnter, onFocus, to, ...rest }: NavLinkProps) {
  const prefetch = useCallback(() => {
    const path = typeof to === 'string' ? to : `${to.pathname || ''}${to.search || ''}`;
    prefetchRoute(path);
  }, [to]);

  return (
    <NavLink
      {...rest}
      to={to}
      onMouseEnter={(e) => {
        prefetch();
        onMouseEnter?.(e);
      }}
      onFocus={(e) => {
        prefetch();
        onFocus?.(e);
      }}
    />
  );
}
