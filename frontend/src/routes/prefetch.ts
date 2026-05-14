/**
 * 与 React.lazy 使用相同的静态 import 路径，便于 hover 预取 chunk。
 */
export function prefetchRoute(pathname: string): void {
  const path = pathname.split('?')[0] || '';

  if (path === '/' || path === '/discover' || path.startsWith('/explore')) {
    void import('../pages/Explore');
    if (path.startsWith('/explore/people')) void import('../pages/SuggestPeople');
    return;
  }
  if (path === '/search') {
    void import('../pages/SearchPage');
    return;
  }
  if (path === '/login') {
    void import('../pages/Login');
    return;
  }
  if (path.startsWith('/blog/')) {
    void import('../pages/BlogDetail');
    return;
  }
  if (path.startsWith('/shop/')) {
    void import('../pages/ShopDetail');
    return;
  }
  if (path.startsWith('/user/')) {
    void import('../pages/UserPage');
    return;
  }
  if (path === '/note/create' || path === '/publish') {
    void import('../pages/Publish');
    return;
  }
  if (path === '/following' || path === '/feed') {
    void import('../pages/FollowFeed');
    return;
  }
  if (path.startsWith('/my/')) {
    void import('../pages/my/Overview');
    void import('../pages/my/Orders');
    void import('../pages/my/Vouchers');
    void import('../pages/my/Reviews');
    void import('../pages/my/Favorites');
    void import('../pages/my/History');
    void import('../pages/my/Points');
    void import('../pages/my/Address');
    void import('../pages/my/Settings');
    void import('../pages/my/Legal');
    return;
  }
  if (path === '/my') {
    void import('../pages/my/Overview');
    return;
  }
  if (path.startsWith('/profile/')) {
    void import('../pages/MyFollowing');
  }
}
