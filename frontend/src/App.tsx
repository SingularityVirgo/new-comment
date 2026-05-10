import { Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import { Layout } from './components/Layout';
import { RequireAuth } from './components/RequireAuth';
import { BlogDetail } from './pages/BlogDetail';
import { Discover } from './pages/Discover';
import { FollowFeed } from './pages/FollowFeed';
import { Login } from './pages/Login';
import { Profile } from './pages/Profile';
import { Publish } from './pages/Publish';
import { ShopDetail } from './pages/ShopDetail';
import { Shops } from './pages/Shops';
import { UserPage } from './pages/UserPage';

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route element={<Layout />}>
          <Route index element={<Navigate to="/discover" replace />} />
          <Route path="discover" element={<Discover />} />
          <Route path="blog/:id" element={<BlogDetail />} />
          <Route path="shops" element={<Shops />} />
          <Route path="shop/:id" element={<ShopDetail />} />
          <Route path="user/:id" element={<UserPage />} />
          <Route path="login" element={<Login />} />
          <Route
            path="publish"
            element={
              <RequireAuth>
                <Publish />
              </RequireAuth>
            }
          />
          <Route
            path="feed"
            element={
              <RequireAuth>
                <FollowFeed />
              </RequireAuth>
            }
          />
          <Route
            path="profile"
            element={
              <RequireAuth>
                <Profile />
              </RequireAuth>
            }
          />
        </Route>
      </Routes>
    </AuthProvider>
  );
}
