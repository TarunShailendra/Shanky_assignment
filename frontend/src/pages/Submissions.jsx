import { useState, useEffect } from 'react';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';

export default function Submissions() {
  const { user } = useAuth();
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [grading, setGrading] = useState(null); // submission id
  const [grade, setGrade] = useState({ score: '', feedback: '' });

  useEffect(() => {
    api.get('/submissions/student')
      .then(res => setSubmissions(res.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const handleGradeSubmit = async (submissionId) => {
    try {
      const res = await api.put(`/submissions/${submissionId}/grade`, {
        score: parseInt(grade.score),
        feedback: grade.feedback,
      });
      setSubmissions(prev => prev.map(s => s.id === submissionId ? { ...s, ...res.data } : s));
      setGrading(null);
      setGrade({ score: '', feedback: '' });
    } catch (err) {
      alert('Failed to submit grade');
    }
  };

  const getScoreColor = (score, maxScore) => {
    if (score === null || score === undefined) return 'text-slate-400';
    const pct = score / maxScore;
    if (pct >= 0.9) return 'text-emerald-600';
    if (pct >= 0.7) return 'text-amber-600';
    return 'text-red-600';
  };

  if (loading) return (
    <div className="min-h-[60vh] flex items-center justify-center">
      <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-primary-600"></div>
    </div>
  );

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-slate-900">
          {user?.role === 'student' ? 'My Submissions' : 'Student Submissions'}
        </h1>
        <p className="text-slate-500 mt-1">
          {user?.role === 'student'
            ? 'Track your assignment submissions and grades'
            : 'Grade and review student submissions'}
        </p>
      </div>

      {submissions.length === 0 ? (
        <div className="text-center py-16 text-slate-400">
          <div className="text-5xl mb-4">📝</div>
          <p className="text-lg font-medium">No submissions yet</p>
        </div>
      ) : (
        <div className="card overflow-hidden">
          <table className="w-full">
            <thead className="bg-slate-50 border-b border-slate-200">
              <tr>
                {['Assignment', 'Course', 'Student', 'Submitted', 'Score', 'Status', 'Action'].map(h => (
                  <th key={h} className="text-left px-4 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {submissions.map(sub => {
                const isGraded = sub.graded_at !== null;
                return (
                  <tr key={sub.id} className="hover:bg-slate-50 transition-colors">
                    <td className="px-4 py-3">
                      <div className="font-medium text-slate-900 text-sm">{sub.assignment_title}</div>
                      {sub.module_title && (
                        <div className="text-xs text-slate-400">{sub.module_title}</div>
                      )}
                      {sub.submission_text && (
                        <a href={sub.submission_text} target="_blank" rel="noopener noreferrer"
                          className="text-xs text-primary-600 hover:underline mt-0.5 block">
                          View submission ↗
                        </a>
                      )}
                    </td>
                    <td className="px-4 py-3 text-sm text-slate-600">{sub.course_title}</td>
                    {user?.role === 'instructor' && (
                      <td className="px-4 py-3 text-sm text-slate-600">{sub.student_name}</td>
                    )}
                    <td className="px-4 py-3 text-sm text-slate-400">
                      {new Date(sub.submitted_at).toLocaleDateString()}
                    </td>
                    <td className="px-4 py-3">
                      {isGraded ? (
                        <div>
                          <span className={`font-bold ${getScoreColor(sub.score, sub.max_score)}`}>
                            {sub.score}
                          </span>
                          <span className="text-slate-400"> / {sub.max_score}</span>
                        </div>
                      ) : (
                        <span className="text-sm text-amber-600 font-medium">Pending</span>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      <span className={`text-xs font-medium px-2 py-1 rounded-full ${
                        isGraded ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'
                      }`}>
                        {isGraded ? 'Graded' : 'Awaiting'}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      {user?.role === 'instructor' && (
                        grading === sub.id ? (
                          <div className="space-y-2">
                            <div className="flex gap-2">
                              <input
                                type="number"
                                min="0"
                                max={sub.max_score}
                                placeholder={`Score (${sub.max_score})`}
                                className="input text-sm py-1 w-24"
                                value={grade.score}
                                onChange={e => setGrade({ ...grade, score: e.target.value })}
                              />
                              <button
                                onClick={() => handleGradeSubmit(sub.id)}
                                disabled={!grade.score}
                                className="btn-primary text-xs py-1 px-3"
                              >
                                Save
                              </button>
                              <button
                                onClick={() => { setGrading(null); setGrade({ score: '', feedback: '' }); }}
                                className="btn-secondary text-xs py-1 px-3"
                              >
                                ✕
                              </button>
                            </div>
                            <textarea
                              placeholder="Feedback (optional)"
                              className="input text-xs py-1 w-full"
                              rows="2"
                              value={grade.feedback}
                              onChange={e => setGrade({ ...grade, feedback: e.target.value })}
                            />
                          </div>
                        ) : (
                          <button
                            onClick={() => { setGrading(sub.id); setGrade({ score: '', feedback: '' }); }}
                            className="text-xs font-medium text-primary-600 hover:text-primary-700 border border-primary-200 hover:border-primary-300 px-3 py-1.5 rounded-lg transition-colors"
                          >
                            {isGraded ? 'Re-grade' : 'Grade'}
                          </button>
                        )
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* Feedback panel for graded items */}
      {submissions.some(s => s.feedback) && (
        <div className="mt-6">
          <h2 className="text-lg font-semibold text-slate-800 mb-4">Instructor Feedback</h2>
          <div className="space-y-3">
            {submissions.filter(s => s.feedback).map(sub => (
              <div key={sub.id} className="card p-5 border-l-4 border-primary-400">
                <div className="flex items-start justify-between gap-4 mb-2">
                  <div className="font-medium text-slate-800 text-sm">{sub.assignment_title}</div>
                  <div className="text-sm font-bold text-emerald-600">{sub.score}/{sub.max_score}</div>
                </div>
                <p className="text-sm text-slate-600">{sub.feedback}</p>
                <p className="text-xs text-slate-400 mt-2">
                  Graded on {new Date(sub.graded_at).toLocaleDateString()}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}