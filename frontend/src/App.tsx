import { Suspense } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import { Layout } from './components/Layout';
import { PageSkeleton } from './components/PageSkeleton';
import { RequireAuth } from './components/RequireAuth';
import {
  BlogDetailPage,
  DiscoverPage,
  FollowFeedPage,
  LoginPage,
  MyFollowingPage,
  ProfilePage,
  PublishPage,
  ShopDetailPage,
  ShopsPage,
  UserPagePage,
} from './routes/lazyPages';

export default function App() {
  return (
    <AuthProvider>
      <Suspense fallback={<PageSkeleton variant="list" />}>
        <Routes>
          <Route element={<Layout />}>
            <Route index element={<Navigate to="/discover" replace />} />
            <Route path="discover" element={<DiscoverPage />} />
            <Route path="blog/:id" element={<BlogDetailPage />} />
            <Route path="shops" element={<ShopsPage />} />
            <Route path="shop/:id" element={<ShopDetailPage />} />
            <Route path="user/:id" element={<UserPagePage />} />
            <Route path="login" element={<LoginPage />} />
            <Route
              path="publish"
              element={
                <RequireAuth>
                  <PublishPage />
                </RequireAuth>
              }
            />
            <Route
              path="feed"
              element={
                <RequireAuth>
                  <FollowFeedPage />
                </RequireAuth>
              }
            />
            <Route
              path="profile"
              element={
                <RequireAuth>
                  <ProfilePage />
                </RequireAuth>
              }
            />
            <Route
              path="profile/following"
              element={
                <RequireAuth>
                  <MyFollowingPage />
                </RequireAuth>
              }
            />
          </Route>
        </Routes>
      </Suspense>
    </AuthProvider>
  );
}
