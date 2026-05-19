import { useState, useEffect } from 'react';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';

export default function InstructorDashboard() {
  const { user } = useAuth();
  const [courses, setCourses] = useState([]);
  const [payments, setPayments] = useState([]);
  const [summary, setSummary] = useState({ total_revenue: 0, total_transactions: 0, total_students: 0 });
  const [showModal, setShowModal] = useState(false);
  const [newCourse, setNewCourse] = useState({ title: '', description: '', price: '', category: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    Promise.all([
      api.get('/courses/instructor/mine'),
      api.get('/payments/instructor'),
      api.get('/payments/summary'),
    ]).then(([crs, pym, smm]) => {
      setCourses(crs.data);
      setPayments(pym.data);
      setSummary(smm.data);
    }).catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const handleCreateCourse = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const res = await api.post('/courses', { ...newCourse, price: parseFloat(newCourse.price) || 0 });
      setCourses(prev => [res.data, ...prev]);
      setShowModal(false);
      setNewCourse({ title: '', description: '', price: '', category: '' });
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to create course');
    } finally {
      setSaving(false);
    }
  };

  const togglePublished = async (course, currentStatus) => {
    try {
      await api.put(`/courses/${course.id}`, { is_published: !currentStatus });
      setCourses(prev => prev.map(c => c.id === course.id ? { ...c, is_published: !currentStatus } : c));
    } catch (err) {
      alert('Failed to update course');
    }
  };

  if (loading) return (
    <div className="min-h-[60vh] flex items-center justify-center">
      <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-primary-600"></div>
    </div>
  );

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Instructor Dashboard</h1>
          <p className="text-slate-500 mt-1">Manage your courses and students</p>
        </div>
        <button onClick={() => setShowModal(true)} className="btn-primary flex items-center gap-2">
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          New Course
        </button>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
        <div className="card p-5">
          <div className="text-3xl font-bold text-emerald-600">${Number(summary.total_revenue).toFixed(2)}</div>
          <div className="text-sm text-slate-500 mt-1">Total Revenue</div>
        </div>
        <div className="card p-5">
          <div className="text-3xl font-bold text-primary-600">{courses.length}</div>
          <div className="text-sm text-slate-500 mt-1">Total Courses</div>
        </div>
        <div className="card p-5">
          <div className="text-3xl font-bold text-amber-600">{summary.total_students}</div>
          <div className="text-sm text-slate-500 mt-1">Total Students</div>
        </div>
      </div>

      {/* Courses */}
      <h2 className="text-lg font-semibold text-slate-800 mb-4">Your Courses</h2>
      {courses.length === 0 ? (
        <div className="text-center py-16 text-slate-400 card p-16">
          <div className="text-5xl mb-4">🎓</div>
          <p className="text-lg font-medium">No courses yet</p>
          <p className="mt-1">Create your first course to get started</p>
        </div>
      ) : (
        <div className="space-y-4">
          {courses.map(course => (
            <div key={course.id} className="card p-6">
              <div className="flex flex-col sm:flex-row gap-4">
                <div className="w-full sm:w-48 h-28 bg-gradient-to-br from-primary-100 to-primary-200 rounded-xl overflow-hidden flex-shrink-0">
                  {course.thumbnail && (
                    <img src={course.thumbnail} alt="" className="w-full h-full object-cover"
                      onError={e => e.target.style.display = 'none'} />
                  )}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex flex-wrap items-start justify-between gap-2 mb-2">
                    <h3 className="font-semibold text-slate-900 text-lg truncate">{course.title}</h3>
                    <span className={`text-xs font-medium px-3 py-1 rounded-full flex-shrink-0 ${
                      course.is_published ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-500'
                    }`}>
                      {course.is_published ? 'Published' : 'Draft'}
                    </span>
                  </div>
                  <p className="text-sm text-slate-500 line-clamp-1 mb-3">{course.description}</p>
                  <div className="flex flex-wrap gap-4 text-sm text-slate-400">
                    <span>{course.student_count} students</span>
                    <span>{course.module_count} modules</span>
                    <span className="font-medium text-slate-600">${Number(course.price).toFixed(2)}</span>
                  </div>
                  <div className="flex gap-2 mt-3">
                    <button
                      onClick={() => togglePublished(course, course.is_published)}
                      className="text-xs font-medium px-3 py-1.5 bg-slate-100 hover:bg-slate-200 rounded-lg transition-colors text-slate-700"
                    >
                      {course.is_published ? 'Unpublish' : 'Publish'}
                    </button>
                    <a
                      href={`/courses/${course.id}`}
                      className="text-xs font-medium px-3 py-1.5 bg-primary-50 hover:bg-primary-100 rounded-lg transition-colors text-primary-700"
                    >
                      View Course
                    </a>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Recent Payments */}
      {payments.length > 0 && (
        <>
          <h2 className="text-lg font-semibold text-slate-800 mt-10 mb-4">Recent Payments</h2>
          <div className="card overflow-hidden">
            <table className="w-full">
              <thead className="bg-slate-50 border-b border-slate-200">
                <tr>
                  {['Student', 'Course', 'Amount', 'Date', 'Method'].map(h => (
                    <th key={h} className="text-left px-4 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {payments.slice(0, 5).map(p => (
                  <tr key={p.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3 text-sm text-slate-900">{p.student_name}</td>
                    <td className="px-4 py-3 text-sm text-slate-600">{p.course_title}</td>
                    <td className="px-4 py-3 text-sm font-medium text-emerald-700">${Number(p.amount).toFixed(2)}</td>
                    <td className="px-4 py-3 text-sm text-slate-500">{new Date(p.paid_at).toLocaleDateString()}</td>
                    <td className="px-4 py-3 text-sm text-slate-500 capitalize">{p.payment_method}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      {/* Create Course Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 px-4" onClick={() => setShowModal(false)}>
          <div className="bg-white rounded-2xl p-8 w-full max-w-lg" onClick={e => e.stopPropagation()}>
            <h2 className="text-xl font-bold text-slate-900 mb-1">Create New Course</h2>
            <p className="text-sm text-slate-500 mb-6">Fill in the details to get started</p>
            <form onSubmit={handleCreateCourse} className="space-y-4">
              <div>
                <label className="label">Course Title *</label>
                <input
                  type="text"
                  className="input"
                  placeholder="e.g. Advanced React Patterns"
                  value={newCourse.title}
                  onChange={e => setNewCourse({ ...newCourse, title: e.target.value })}
                  required
                />
              </div>
              <div>
                <label className="label">Description</label>
                <textarea
                  className="input"
                  rows="3"
                  placeholder="What will students learn?"
                  value={newCourse.description}
                  onChange={e => setNewCourse({ ...newCourse, description: e.target.value })}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="label">Price ($)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    className="input"
                    placeholder="99.99"
                    value={newCourse.price}
                    onChange={e => setNewCourse({ ...newCourse, price: e.target.value })}
                  />
                </div>
                <div>
                  <label className="label">Category</label>
                  <input
                    type="text"
                    className="input"
                    placeholder="e.g. Web Development"
                    value={newCourse.category}
                    onChange={e => setNewCourse({ ...newCourse, category: e.target.value })}
                  />
                </div>
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setShowModal(false)} className="btn-secondary flex-1">Cancel</button>
                <button type="submit" disabled={saving} className="btn-primary flex-1">
                  {saving ? 'Creating...' : 'Create Course'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}