const express = require('express');
const pool = require('../db');
const { requireAuth } = require('./courses');

const router = express.Router();

// GET /api/submissions/student — all submissions for the current student
router.get('/student', requireAuth, async (req, res) => {
  try {
    if (req.user.role === 'student') {
      const result = await pool.query(`
        SELECT s.*, a.title as assignment_title, a.description as assignment_desc,
               a.due_date, a.max_score,
               m.title as module_title, c.title as course_title
        FROM submissions s
        JOIN assignments a ON a.id = s.assignment_id
        JOIN modules m ON m.id = a.module_id
        JOIN courses c ON c.id = m.course_id
        WHERE s.student_id = $1
        ORDER BY s.submitted_at DESC
      `, [req.user.profile_id]);
      return res.json(result.rows);
    }

    // Instructor: all submissions for their assignments
    const result = await pool.query(`
      SELECT s.*, a.title as assignment_title, a.description as assignment_desc,
             a.due_date, a.max_score,
             m.title as module_title, c.title as course_title,
             u.full_name as student_name
      FROM submissions s
      JOIN assignments a ON a.id = s.assignment_id
      JOIN modules m ON m.id = a.module_id
      JOIN courses c ON c.id = m.course_id
      JOIN students st ON st.id = s.student_id
      JOIN users u ON u.id = st.user_id
      WHERE c.instructor_id = $1
      ORDER BY s.submitted_at DESC
    `, [req.user.profile_id]);
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to fetch submissions' });
  }
});

// GET /api/submissions/assignment/:id — submissions for an assignment (instructor view)
router.get('/assignment/:id', requireAuth, async (req, res) => {
  try {
    const assignment = await pool.query(`
      SELECT a.*, m.course_id, c.instructor_id
      FROM assignments a
      JOIN modules m ON m.id = a.module_id
      JOIN courses c ON c.id = m.course_id
      WHERE a.id = $1
    `, [req.params.id]);

    if (!assignment.rows.length) return res.status(404).json({ error: 'Assignment not found' });

    const result = await pool.query(`
      SELECT s.*, u.full_name as student_name
      FROM submissions s
      JOIN students st ON st.id = s.student_id
      JOIN users u ON u.id = st.user_id
      WHERE s.assignment_id = $1
      ORDER BY s.submitted_at
    `, [req.params.id]);

    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to fetch assignments submissions' });
  }
});

// POST /api/submissions — submit an assignment
router.post('/', requireAuth, async (req, res) => {
  if (req.user.role !== 'student') {
    return res.status(403).json({ error: 'Only students can submit' });
  }

  const { assignment_id, submission_text, file_url } = req.body;
  if (!assignment_id) {
    return res.status(400).json({ error: 'assignment_id is required' });
  }

  try {
    const result = await pool.query(`
      INSERT INTO submissions (assignment_id, student_id, submission_text, file_url)
      VALUES ($1, $2, $3, $4)
      ON CONFLICT (assignment_id, student_id)
      DO UPDATE SET submission_text = $3, file_url = $4, submitted_at = CURRENT_TIMESTAMP
      RETURNING *
    `, [assignment_id, req.user.profile_id, submission_text || '', file_url || '']);

    res.status(201).json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to submit assignment' });
  }
});

// PUT /api/submissions/:id/grade — grade a submission (instructor only)
router.put('/:id/grade', requireAuth, async (req, res) => {
  if (req.user.role !== 'instructor') {
    return res.status(403).json({ error: 'Only instructors can grade' });
  }

  const { score, feedback } = req.body;
  if (score === undefined) {
    return res.status(400).json({ error: 'score is required' });
  }

  try {
    const owner = await pool.query(`
      SELECT c.instructor_id FROM submissions s
      JOIN assignments a ON a.id = s.assignment_id
      JOIN modules m ON m.id = a.module_id
      JOIN courses c ON c.id = m.course_id
      WHERE s.id = $1
    `, [req.params.id]);

    if (!owner.rows.length) return res.status(404).json({ error: 'Submission not found' });
    if (owner.rows[0].instructor_id !== req.user.profile_id) {
      return res.status(403).json({ error: 'Forbidden' });
    }

    const result = await pool.query(`
      UPDATE submissions SET score = $1, feedback = $2, graded_at = CURRENT_TIMESTAMP
      WHERE id = $3
      RETURNING *
    `, [score, feedback || '', req.params.id]);

    res.json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to grade submission' });
  }
});

// GET /api/submissions/:id
router.get('/:id', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT s.*, u.full_name as student_name, a.title as assignment_title,
             a.max_score, m.title as module_title, c.title as course_title
      FROM submissions s
      JOIN students st ON st.id = s.student_id
      JOIN users u ON u.id = st.user_id
      JOIN assignments a ON a.id = s.assignment_id
      JOIN modules m ON m.id = a.module_id
      JOIN courses c ON c.id = m.course_id
      WHERE s.id = $1
    `, [req.params.id]);

    if (!result.rows.length) return res.status(404).json({ error: 'Submission not found' });
    res.json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to fetch submission' });
  }
});

module.exports = router;