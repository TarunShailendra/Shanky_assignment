import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Layout from './components/Layout';
import Login from './pages/Login';
import Register from './pages/Register';
import StudentDashboard from './pages/StudentDashboard';
import InstructorDashboard from './pages/InstructorDashboard';
import CourseDetail from './pages/CourseDetail';
import Submissions from './pages/Submissions';
import Payments from './pages/Payments';

function ProtectedRoute({ children, roles }) {
  const { user, loading } = useAuth();
  if (loading) return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-primary-600"></div>
    </div>
  );
  if (!user) return <Navigate to="/login" replace />;
  if (roles && !roles.includes(user.role)) {
    return <Navigate to={user.role === 'instructor' ? '/instructor' : '/dashboard'} replace />;
  }
  return children;
}

function AppRoutes() {
  const { user, loading } = useAuth();

  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to={user.role === 'instructor' ? '/instructor' : '/dashboard'} replace /> : <Login />} />
      <Route path="/register" element={user ? <Navigate to={user.role === 'instructor' ? '/instructor' : '/dashboard'} replace /> : <Register />} />

      <Route path="/" element={<Layout />}>
        <Route index element={
          user
            ? <Navigate to={user.role === 'instructor' ? '/instructor' : '/dashboard'} replace />
            : <Navigate to="/login" replace />
        } />

        <Route path="dashboard" element={
          <ProtectedRoute roles={['student']}><StudentDashboard /></ProtectedRoute>
        } />
        <Route path="instructor" element={
          <ProtectedRoute roles={['instructor']}><InstructorDashboard /></ProtectedRoute>
        } />
        <Route path="courses/:id" element={<CourseDetail />} />
        <Route path="submissions" element={
          <ProtectedRoute><Submissions /></ProtectedRoute>
        } />
        <Route path="payments" element={
          <ProtectedRoute><Payments /></ProtectedRoute>
        } />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}