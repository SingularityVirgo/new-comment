import { lazy } from 'react';

export const ExplorePage = lazy(() => import('../pages/Explore').then((m) => ({ default: m.Explore })));
export const SearchPage = lazy(() => import('../pages/SearchPage').then((m) => ({ default: m.SearchPage })));
export const SuggestPeoplePage = lazy(() => import('../pages/SuggestPeople').then((m) => ({ default: m.SuggestPeople })));
export const BlogDetailPage = lazy(() => import('../pages/BlogDetail').then((m) => ({ default: m.BlogDetail })));
export const ShopDetailPage = lazy(() => import('../pages/ShopDetail').then((m) => ({ default: m.ShopDetail })));
export const UserPagePage = lazy(() => import('../pages/UserPage').then((m) => ({ default: m.UserPage })));
export const LoginPage = lazy(() => import('../pages/Login').then((m) => ({ default: m.Login })));
export const PublishPage = lazy(() => import('../pages/Publish').then((m) => ({ default: m.Publish })));
export const FollowFeedPage = lazy(() => import('../pages/FollowFeed').then((m) => ({ default: m.FollowFeed })));
export const MyFollowingPage = lazy(() => import('../pages/MyFollowing').then((m) => ({ default: m.MyFollowing })));

export const MyOverviewPage = lazy(() => import('../pages/my/Overview').then((m) => ({ default: m.MyOverview })));
export const MyOrdersPage = lazy(() => import('../pages/my/Orders').then((m) => ({ default: m.MyOrders })));
export const MyVouchersPage = lazy(() => import('../pages/my/Vouchers').then((m) => ({ default: m.MyVouchers })));
export const MyReviewsPage = lazy(() => import('../pages/my/Reviews').then((m) => ({ default: m.MyReviews })));
export const MyFavoritesPage = lazy(() => import('../pages/my/Favorites').then((m) => ({ default: m.MyFavorites })));
export const MyHistoryPage = lazy(() => import('../pages/my/History').then((m) => ({ default: m.MyHistory })));
export const MyPointsPage = lazy(() => import('../pages/my/Points').then((m) => ({ default: m.MyPoints })));
export const MyAddressPage = lazy(() => import('../pages/my/Address').then((m) => ({ default: m.MyAddress })));
export const MySettingsPage = lazy(() => import('../pages/my/Settings').then((m) => ({ default: m.MySettings })));
export const MyLegalPage = lazy(() => import('../pages/my/Legal').then((m) => ({ default: m.MyLegal })));
