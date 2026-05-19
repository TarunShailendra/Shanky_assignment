import { useState, useEffect } from 'react';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';

export default function Payments() {
  const { user } = useAuth();
  const [payments, setPayments] = useState([]);
  const [instructorPayments, setInstructorPayments] = useState([]);
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState(user?.role === 'instructor' ? 'instructor' : 'student');

  useEffect(() => {
    Promise.all([
      api.get('/payments/student'),
      user?.role === 'instructor' ? api.get('/payments/instructor') : Promise.resolve({ data: [] }),
      api.get('/payments/summary'),
    ]).then(([stu, inst, smm]) => {
      setPayments(stu.data);
      setInstructorPayments(inst.data);
      setSummary(smm.data);
    }).catch(console.error)
      .finally(() => setLoading(false));
  }, [user]);

  const getStatusColor = (status) => {
    switch (status) {
      case 'completed': return 'bg-emerald-50 text-emerald-700';
      case 'pending': return 'bg-amber-50 text-amber-700';
      case 'failed': return 'bg-red-50 text-red-700';
      case 'refunded': return 'bg-slate-50 text-slate-600';
      default: return 'bg-slate-100 text-slate-600';
    }
  };

  if (loading) return (
    <div className="min-h-[60vh] flex items-center justify-center">
      <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-primary-600"></div>
    </div>
  );

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-slate-900">Payments</h1>
        <p className="text-slate-500 mt-1">
          {user?.role === 'instructor' ? 'Revenue and transaction history' : 'Your payment history'}
        </p>
      </div>

      {/* Summary */}
      {summary && (
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
          {user?.role === 'instructor' ? (
            <>
              <div className="card p-5">
                <div className="text-3xl font-bold text-emerald-600">
                  ${Number(summary.total_revenue || 0).toFixed(2)}
                </div>
                <div className="text-sm text-slate-500 mt-1">Total Revenue</div>
              </div>
              <div className="card p-5">
                <div className="text-3xl font-bold text-primary-600">{summary.total_transactions || 0}</div>
                <div className="text-sm text-slate-500 mt-1">Transactions</div>
              </div>
              <div className="card p-5">
                <div className="text-3xl font-bold text-amber-600">{summary.courses_sold || 0}</div>
                <div className="text-sm text-slate-500 mt-1">Courses Sold</div>
              </div>
            </>
          ) : (
            <>
              <div className="card p-5">
                <div className="text-3xl font-bold text-primary-600">
                  ${Number(summary.total_spent || 0).toFixed(2)}
                </div>
                <div className="text-sm text-slate-500 mt-1">Total Spent</div>
              </div>
              <div className="card p-5">
                <div className="text-3xl font-bold text-slate-700">{summary.total_payments || 0}</div>
                <div className="text-sm text-slate-500 mt-1">Payments Made</div>
              </div>
              <div className="card p-5">
                <div className="text-3xl font-bold text-emerald-600">{summary.courses_purchased || 0}</div>
                <div className="text-sm text-slate-500 mt-1">Courses Purchased</div>
              </div>
            </>
          )}
        </div>
      )}

      {/* Tabs for instructor */}
      {user?.role === 'instructor' && (
        <div className="flex gap-1 mb-6 bg-slate-100 p-1 rounded-xl w-fit">
          {[
            ['instructor', 'My Revenue'],
            ['student', 'All Payments'],
          ].map(([key, label]) => (
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
      )}

      {/* Student payments */}
      {(tab === 'student' || user?.role === 'student') && (
        <>
          {payments.length === 0 ? (
            <div className="text-center py-16 text-slate-400">
              <div className="text-5xl mb-4">💳</div>
              <p className="text-lg font-medium">No payment history</p>
            </div>
          ) : (
            <div className="card overflow-hidden">
              <table className="w-full">
                <thead className="bg-slate-50 border-b border-slate-200">
                  <tr>
                    {['Course', 'Amount', 'Method', 'Status', 'Date', 'Transaction ID'].map(h => (
                      <th key={h} className="text-left px-4 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                        {h}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {payments.map(p => (
                    <tr key={p.id} className="hover:bg-slate-50 transition-colors">
                      <td className="px-4 py-3">
                        <div className="font-medium text-slate-900 text-sm">{p.course_title}</div>
                        {p.course_thumbnail && (
                          <img src={p.course_thumbnail} alt=""
                            className="w-8 h-8 rounded object-cover mt-1"
                            onError={e => e.target.style.display = 'none'} />
                        )}
                      </td>
                      <td className="px-4 py-3 text-sm font-semibold text-slate-900">
                        ${Number(p.amount).toFixed(2)}
                      </td>
                      <td className="px-4 py-3 text-sm text-slate-600 capitalize">{p.payment_method}</td>
                      <td className="px-4 py-3">
                        <span className={`text-xs font-medium px-2 py-1 rounded-full ${getStatusColor(p.status)}`}>
                          {p.status}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-sm text-slate-400">
                        {new Date(p.paid_at).toLocaleDateString()}
                      </td>
                      <td className="px-4 py-3 text-xs text-slate-400 font-mono">
                        {p.transaction_id}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}

      {/* Instructor payments tab */}
      {tab === 'instructor' && user?.role === 'instructor' && (
        <>
          {instructorPayments.length === 0 ? (
            <div className="text-center py-16 text-slate-400">
              <div className="text-5xl mb-4">📊</div>
              <p className="text-lg font-medium">No revenue yet</p>
              <p className="mt-1">Revenue will appear here once students enroll in your courses</p>
            </div>
          ) : (
            <>
              {/* Revenue by course */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                {(() => {
                  const byCourse = instructorPayments.reduce((acc, p) => {
                    if (!acc[p.course_title]) acc[p.course_title] = 0;
                    acc[p.course_title] += Number(p.amount);
                    return acc;
                  }, {});
                  return Object.entries(byCourse).map(([course, revenue]) => (
                    <div key={course} className="card p-5">
                      <div className="text-sm text-slate-500 mb-1">{course}</div>
                      <div className="text-2xl font-bold text-emerald-600">${revenue.toFixed(2)}</div>
                    </div>
                  ));
                })()}
              </div>

              <div className="card overflow-hidden">
                <table className="w-full">
                  <thead className="bg-slate-50 border-b border-slate-200">
                    <tr>
                      {['Student', 'Course', 'Amount', 'Method', 'Date', 'Transaction'].map(h => (
                        <th key={h} className="text-left px-4 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                          {h}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {instructorPayments.map(p => (
                      <tr key={p.id} className="hover:bg-slate-50 transition-colors">
                        <td className="px-4 py-3 text-sm text-slate-900 font-medium">{p.student_name}</td>
                        <td className="px-4 py-3 text-sm text-slate-600">{p.course_title}</td>
                        <td className="px-4 py-3 text-sm font-bold text-emerald-700">${Number(p.amount).toFixed(2)}</td>
                        <td className="px-4 py-3 text-sm text-slate-500 capitalize">{p.payment_method}</td>
                        <td className="px-4 py-3 text-sm text-slate-400">
                          {new Date(p.paid_at).toLocaleDateString()}
                        </td>
                        <td className="px-4 py-3 text-xs text-slate-400 font-mono">{p.transaction_id}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </>
      )}
    </div>
  );
}