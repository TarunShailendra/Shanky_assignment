import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';

export default function CourseDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const [course, setCourse] = useState(null);
  const [enrollment, setEnrollment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [enrolling, setEnrolling] = useState(false);
  const [paying, setPaying] = useState(false);
  const [selectedModule, setSelectedModule] = useState(null);
  const [showPayModal, setShowPayModal] = useState(false);
  const [addModule, setAddModule] = useState(false);
  const [newModule, setNewModule] = useState({ title: '', description: '' });
  const [addAssignment, setAddAssignment] = useState(null); // module id
  const [newAssignment, setNewAssignment] = useState({ title: '', description: '', due_date: '', max_score: '100' });

  useEffect(() => {
    Promise.all([
      api.get(`/courses/${id}`),
      user?.role === 'student'
        ? api.get('/enrollments/student').then(r => r.data.find(e => e.course_id === +id))
        : Promise.resolve(null),
    ]).then(([crs, enr]) => {
      setCourse(crs.data);
      setEnrollment(enr || null);
    }).catch(console.error)
      .finally(() => setLoading(false));
  }, [id, user]);

  const handleEnroll = async () => {
    if (!user) return;
    setShowPayModal(true);
  };

  const handlePayment = async () => {
    if (!user) return;
    setPaying(true);
    try {
      const res = await api.post('/payments', {
        course_id: +id,
        amount: course.price,
        payment_method: 'card',
      });
      setEnrollment({ status: 'active', course_id: +id });
      setShowPayModal(false);
    } catch (err) {
      alert(err.response?.data?.error || 'Payment failed');
    } finally {
      setPaying(false);
    }
  };

  const handleAddModule = async (e) => {
    e.preventDefault();
    try {
      const res = await api.post('/modules', { course_id: +id, ...newModule });
      setCourse(prev => ({ ...prev, modules: [...prev.modules, res.data] }));
      setAddModule(false);
      setNewModule({ title: '', description: '' });
    } catch (err) {
      alert('Failed to add module');
    }
  };

  const handleAddAssignment = async (e) => {
    e.preventDefault();
    try {
      const res = await api.post('/assignments', { module_id: addAssignment, ...newAssignment });
      setCourse(prev => ({
        ...prev,
        modules: prev.modules.map(m => m.id === addAssignment ? {
          ...m,
          assignments: [...(m.assignments || []), res.data],
        } : m),
      }));
      setAddAssignment(null);
      setNewAssignment({ title: '', description: '', due_date: '', max_score: '100' });
    } catch (err) {
      alert('Failed to add assignment');
    }
  };

  const isOwner = user?.role === 'instructor' && course?.instructor_id === user.profile_id;

  if (loading) return (
    <div className="min-h-[60vh] flex items-center justify-center">
      <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-primary-600"></div>
    </div>
  );

  if (!course) return (
    <div className="max-w-7xl mx-auto px-4 py-16 text-center text-slate-400">Course not found</div>
  );

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      {/* Hero */}
      <div className="card overflow-hidden mb-8">
        <div className="h-64 bg-gradient-to-br from-primary-600 to-primary-800 relative">
          {course.thumbnail && (
            <img src={course.thumbnail} alt="" className="w-full h-full object-cover opacity-30"
              onError={e => e.target.style.display = 'none'} />
          )}
          <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent"></div>
          <div className="absolute bottom-0 left-0 right-0 p-8">
            <div className="flex flex-wrap items-start justify-between gap-4">
              <div>
                <span className="text-xs font-medium text-primary-200 bg-white/20 px-3 py-1 rounded-full">
                  {course.category}
                </span>
                <h1 className="text-3xl font-bold text-white mt-3">{course.title}</h1>
                <p className="text-primary-100 mt-1">by {course.instructor_name}</p>
                {course.expertise && (
                  <p className="text-sm text-primary-200 mt-1">{course.expertise}</p>
                )}
              </div>
              <div className="text-right">
                <div className="text-3xl font-bold text-white">${Number(course.price).toFixed(2)}</div>
                {!enrollment && !isOwner && (user?.role === 'student') && (
                  <button onClick={handleEnroll} className="mt-3 btn-primary bg-white !text-primary-700 hover:bg-primary-50">
                    Enroll Now
                  </button>
                )}
                {enrollment && (
                  <div className="mt-3 text-sm font-medium text-emerald-300">✓ Enrolled</div>
                )}
              </div>
            </div>
          </div>
        </div>
        <div className="p-8">
          <h2 className="font-semibold text-slate-800 mb-3">About this course</h2>
          <p className="text-slate-600 leading-relaxed">{course.description}</p>
          {course.bio && (
            <div className="mt-6 p-4 bg-slate-50 rounded-xl">
              <h3 className="font-semibold text-slate-800 mb-1">About the Instructor</h3>
              <p className="text-sm text-slate-600">{course.bio}</p>
            </div>
          )}
        </div>
      </div>

      {/* Modules */}
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-xl font-bold text-slate-900">Course Content</h2>
        {isOwner && (
          <button onClick={() => setAddModule(true)} className="btn-outline text-sm py-2">
            + Add Module
          </button>
        )}
      </div>

      {course.modules?.length === 0 && (
        <div className="text-center py-12 text-slate-400 card p-12">
          <p className="text-lg font-medium">No modules yet</p>
          {isOwner && <p className="mt-1 text-sm text-primary-500">Add the first module to get started</p>}
        </div>
      )}

      <div className="space-y-3">
        {course.modules?.sort((a, b) => a.order_index - b.order_index).map(mod => (
          <div key={mod.id} className="card overflow-hidden">
            <button
              className="w-full text-left px-5 py-4 flex items-center justify-between hover:bg-slate-50 transition-colors"
              onClick={() => setSelectedModule(selectedModule === mod.id ? null : mod.id)}
            >
              <div className="flex items-center gap-3">
                <span className="w-8 h-8 rounded-full bg-primary-100 text-primary-700 flex items-center justify-center text-sm font-bold flex-shrink-0">
                  {mod.order_index}
                </span>
                <div>
                  <div className="font-medium text-slate-900">{mod.title}</div>
                  <div className="text-sm text-slate-400 mt-0.5">
                    {mod.assignment_count} assignment{mod.assignment_count !== 1 ? 's' : ''}
                  </div>
                </div>
              </div>
              <svg className={`w-5 h-5 text-slate-400 transition-transform ${selectedModule === mod.id ? 'rotate-180' : ''}`}
                fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
              </svg>
            </button>

            {selectedModule === mod.id && (
              <div className="border-t border-slate-200 px-5 py-4 bg-slate-50">
                {mod.description && (
                  <p className="text-sm text-slate-600 mb-3">{mod.description}</p>
                )}

                {mod.assignments?.length > 0 && (
                  <div className="space-y-2 mb-3">
                    {mod.assignments.map(a => (
                      <div key={a.id} className="flex items-center gap-3 bg-white rounded-lg p-3 border border-slate-200">
                        <span className="text-lg">📋</span>
                        <div className="flex-1 min-w-0">
                          <div className="font-medium text-slate-800 text-sm">{a.title}</div>
                          {a.due_date && (
                            <div className="text-xs text-slate-400 mt-0.5">
                              Due: {new Date(a.due_date).toLocaleDateString()}
                            </div>
                          )}
                        </div>
                        <span className="text-xs bg-slate-100 text-slate-500 px-2 py-1 rounded">
                          {a.max_score}pts
                        </span>
                      </div>
                    ))}
                  </div>
                )}

                {isOwner && (
                  <div>
                    <button
                      onClick={() => setAddAssignment(mod.id)}
                      className="text-sm text-primary-600 hover:text-primary-700 font-medium"
                    >
                      + Add Assignment
                    </button>
                  </div>
                )}

                {addAssignment === mod.id && (
                  <form onSubmit={handleAddAssignment} className="mt-3 space-y-3 bg-white rounded-lg p-4 border border-slate-200">
                    <input
                      type="text"
                      placeholder="Assignment title"
                      className="input text-sm"
                      value={newAssignment.title}
                      onChange={e => setNewAssignment({ ...newAssignment, title: e.target.value })}
                      required
                    />
                    <textarea
                      placeholder="Description"
                      className="input text-sm"
                      value={newAssignment.description}
                      onChange={e => setNewAssignment({ ...newAssignment, description: e.target.value })}
                    />
                    <div className="grid grid-cols-2 gap-2">
                      <input
                        type="date"
                        className="input text-sm"
                        value={newAssignment.due_date}
                        onChange={e => setNewAssignment({ ...newAssignment, due_date: e.target.value })}
                      />
                      <input
                        type="number"
                        placeholder="Max score"
                        className="input text-sm"
                        value={newAssignment.max_score}
                        onChange={e => setNewAssignment({ ...newAssignment, max_score: e.target.value })}
                      />
                    </div>
                    <div className="flex gap-2">
                      <button type="submit" className="btn-primary text-sm py-1.5">Save</button>
                      <button type="button" onClick={() => setAddAssignment(null)} className="btn-secondary text-sm py-1.5">Cancel</button>
                    </div>
                  </form>
                )}
              </div>
            )}
          </div>
        ))}
      </div>

      {/* Add Module Modal */}
      {addModule && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 px-4" onClick={() => setAddModule(false)}>
          <div className="bg-white rounded-2xl p-8 w-full max-w-lg" onClick={e => e.stopPropagation()}>
            <h2 className="text-xl font-bold text-slate-900 mb-4">Add New Module</h2>
            <form onSubmit={handleAddModule} className="space-y-4">
              <div>
                <label className="label">Module Title *</label>
                <input type="text" className="input" value={newModule.title}
                  onChange={e => setNewModule({ ...newModule, title: e.target.value })} required />
              </div>
              <div>
                <label className="label">Description</label>
                <textarea className="input" rows="3" value={newModule.description}
                  onChange={e => setNewModule({ ...newModule, description: e.target.value })} />
              </div>
              <div className="flex gap-3">
                <button type="button" onClick={() => setAddModule(false)} className="btn-secondary flex-1">Cancel</button>
                <button type="submit" className="btn-primary flex-1">Add Module</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Payment Modal */}
      {showPayModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 px-4" onClick={() => setShowPayModal(false)}>
          <div className="bg-white rounded-2xl p-8 w-full max-w-md" onClick={e => e.stopPropagation()}>
            <h2 className="text-xl font-bold text-slate-900 mb-1">Confirm Enrollment</h2>
            <p className="text-sm text-slate-500 mb-6">You will be enrolled after payment</p>
            <div className="bg-slate-50 rounded-xl p-4 mb-6">
              <div className="flex justify-between mb-2">
                <span className="text-slate-600">{course.title}</span>
                <span className="font-semibold">${Number(course.price).toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-sm text-slate-400">
                <span>Payment method</span>
                <span>Card</span>
              </div>
            </div>
            <button onClick={handlePayment} disabled={paying} className="btn-primary w-full py-3">
              {paying ? 'Processing...' : `Pay $${Number(course.price).toFixed(2)}`}
            </button>
            <button onClick={() => setShowPayModal(false)} className="w-full mt-3 text-sm text-slate-500">Cancel</button>
          </div>
        </div>
      )}
    </div>
  );
}