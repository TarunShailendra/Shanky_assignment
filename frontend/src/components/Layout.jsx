import { Outlet, Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Layout() {
  const { user, logout } = useAuth();
  const location = useLocation();

  const navLinks = user ? [
    ...(user.role === 'student' ? [
      { to: '/dashboard', label: 'My Courses' },
      { to: '/submissions', label: 'Submissions' },
      { to: '/payments', label: 'Payments' },
    ] : []),
    ...(user.role === 'instructor' ? [
      { to: '/instructor', label: 'Dashboard' },
      { to: '/submissions', label: 'Submissions' },
      { to: '/payments', label: 'Payments' },
    ] : []),
  ] : [];

  return (
    <div className="min-h-screen flex flex-col">
      <header className="bg-white shadow-sm border-b border-slate-200 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center gap-8">
              <Link to="/" className="flex items-center gap-2 text-xl font-bold text-primary-700">
                <svg className="w-8 h-8 text-primary-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                </svg>
                CourseHub
              </Link>
              {user && (
                <nav className="hidden md:flex items-center gap-1">
                  {navLinks.map(link => (
                    <Link
                      key={link.to}
                      to={link.to}
                      className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                        location.pathname === link.to
                          ? 'bg-primary-50 text-primary-700'
                          : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                      }`}
                    >
                      {link.label}
                    </Link>
                  ))}
                </nav>
              )}
            </div>

            <div className="flex items-center gap-3">
              {user ? (
                <div className="flex items-center gap-3">
                  <div className="text-right hidden sm:block">
                    <div className="text-sm font-medium text-slate-900">{user.full_name}</div>
                    <div className="text-xs text-slate-500 capitalize">{user.role}</div>
                  </div>
                  <button
                    onClick={logout}
                    className="text-sm text-slate-500 hover:text-red-600 transition-colors font-medium"
                  >
                    Logout
                  </button>
                </div>
              ) : (
                <div className="flex items-center gap-2">
                  <Link to="/login" className="px-4 py-2 text-sm font-medium text-slate-600 hover:text-primary-700 transition-colors">
                    Sign In
                  </Link>
                  <Link to="/register" className="btn-primary text-sm py-2 px-4">
                    Sign Up
                  </Link>
                </div>
              )}
            </div>
          </div>
        </div>
      </header>

      <main className="flex-1">
        <Outlet />
      </main>

      <footer className="bg-white border-t border-slate-200 mt-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <p className="text-center text-sm text-slate-500">
            © 2026 CourseHub. Built for online learning.
          </p>
        </div>
      </footer>
    </div>
  );
}