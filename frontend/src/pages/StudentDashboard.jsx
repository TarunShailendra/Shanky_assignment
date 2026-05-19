import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';

export default function StudentDashboard() {
  const { user } = useAuth();
  const [enrollments, setEnrollments] = useState([]);
  const [courses, setCourses] = useState([]);
  const [summary, setSummary] = useState({ total_spent: 0, total_payments: 0, courses_purchased: 0 });
  const [tab, setTab] = useState('enrolled');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.get('/enrollments/student'),
      api.get('/courses'),
      api.get('/payments/summary'),
    ]).then(([enr, crs, smm]) => {
      setEnrollments(enr.data);
      setCourses(crs.data);
      setSummary(smm.data);
    }).catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const availableCourses = courses.filter(
    c => !enrollments.find(e => e.course_id === c.id)
  );

  if (loading) return (
    <div className="min-h-[60vh] flex items-center justify-center">
      <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-primary-600"></div>
    </div>
  );

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-slate-900">Welcome back, {user?.full_name?.split(' ')[0]}!</h1>
        <p className="text-slate-500 mt-1">Continue your learning journey</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
        <div className="card p-5">
          <div className="text-3xl font-bold text-primary-600">{enrollments.length}</div>
          <div className="text-sm text-slate-500 mt-1">Enrolled Courses</div>
        </div>
        <div className="card p-5">
          <div className="text-3xl font-bold text-emerald-600">${Number(summary.total_spent).toFixed(2)}</div>
          <div className="text-sm text-slate-500 mt-1">Total Spent</div>
        </div>
        <div className="card p-5">
          <div className="text-3xl font-bold text-amber-600">{summary.courses_purchased}</div>
          <div className="text-sm text-slate-500 mt-1">Payments Made</div>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 mb-6 bg-slate-100 p-1 rounded-xl w-fit">
        {[['enrolled', 'My Courses'], ['browse', 'Browse Courses']].map(([key, label]) => (
          <button
            key={key}
            onClick={() => setTab(key)}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
              tab === key ? 'bg-white text-primary-700 shadow-sm' : 'text-slate-500 hover:text-slate-700'
            }`}
          >
            {label}
          </button>
        ))}
      </div>

      {tab === 'enrolled' && (
        enrollments.length === 0 ? (
          <div className="text-center py-16 text-slate-400">
            <div className="text-5xl mb-4">📚</div>
            <p className="text-lg font-medium">No courses yet</p>
            <p className="mt-1">Browse courses and enroll to get started</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {enrollments.map(enr => (
              <Link key={enr.id} to={`/courses/${enr.course_id}`} className="card group">
                <div className="h-40 bg-gradient-to-br from-primary-100 to-primary-200 overflow-hidden">
                  {enr.thumbnail && (
                    <img
                      src={enr.thumbnail}
                      alt={enr.title}
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                      onError={e => e.target.style.display = 'none'}
                    />
                  )}
                </div>
                <div className="p-5">
                  <div className="flex items-start justify-between gap-2 mb-2">
                    <span className="text-xs font-medium text-primary-600 bg-primary-50 px-2 py-1 rounded-full">
                      {enr.category}
                    </span>
                    <span className={`text-xs font-medium px-2 py-1 rounded-full ${
                      enr.status === 'completed' ? 'bg-emerald-50 text-emerald-700' :
                      enr.status === 'active' ? 'bg-blue-50 text-blue-700' :
                      'bg-slate-100 text-slate-500'
                    }`}>
                      {enr.status}
                    </span>
                  </div>
                  <h3 className="font-semibold text-slate-900 line-clamp-2 mb-1">{enr.title}</h3>
                  <p className="text-sm text-slate-500 mb-3">by {enr.instructor_name}</p>
                  <div className="flex items-center gap-4 text-xs text-slate-400">
                    <span>{enr.module_count} modules</span>
                    <span>{enr.assignment_count} assignments</span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )
      )}

      {tab === 'browse' && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {availableCourses.map(course => (
            <CourseCard key={course.id} course={course} />
          ))}
          {availableCourses.length === 0 && (
            <div className="col-span-full text-center py-16 text-slate-400">
              <p className="text-lg font-medium">No new courses available</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function CourseCard({ course }) {
  return (
    <div className="card overflow-hidden">
      <div className="h-40 bg-gradient-to-br from-slate-100 to-slate-200 overflow-hidden">
        {course.thumbnail && (
          <img
            src={course.thumbnail}
            alt={course.title}
            className="w-full h-full object-cover"
            onError={e => e.target.style.display = 'none'}
          />
        )}
      </div>
      <div className="p-5">
        <span className="text-xs font-medium text-slate-500 bg-slate-100 px-2 py-1 rounded-full">
          {course.category}
        </span>
        <h3 className="font-semibold text-slate-900 mt-2 mb-1 line-clamp-2">{course.title}</h3>
        <p className="text-sm text-slate-500 mb-2">by {course.instructor_name}</p>
        <p className="text-sm text-slate-400 line-clamp-2 mb-4">{course.description}</p>
        <div className="flex items-center justify-between mt-auto">
          <span className="text-lg font-bold text-primary-700">${Number(course.price).toFixed(2)}</span>
          <Link to={`/courses/${course.id}`} className="btn-primary text-sm py-2 px-4">
            Enroll
          </Link>
        </div>
      </div>
    </div>
  );
}