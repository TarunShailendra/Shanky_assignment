const express = require('express');
const pool = require('../db');
const { requireAuth } = require('./courses');

const router = express.Router();

// GET /api/enrollments/student
router.get('/student', requireAuth, async (req, res) => {
  if (req.user.role !== 'student') {
    return res.status(403).json({ error: 'Forbidden' });
  }

  try {
    const result = await pool.query(`
      SELECT e.*, c.title, c.description, c.thumbnail, c.price, c.category,
             u.full_name as instructor_name,
             (SELECT COUNT(*) FROM modules WHERE course_id = c.id) as module_count,
             (SELECT COUNT(*) FROM assignments a
              JOIN modules m ON m.id = a.module_id
              WHERE m.course_id = c.id) as assignment_count
      FROM enrollments e
      JOIN courses c ON c.id = e.course_id
      JOIN instructors i ON i.id = c.instructor_id
      JOIN users u ON u.id = i.user_id
      WHERE e.student_id = $1
      ORDER BY e.enrolled_at DESC
    `, [req.user.profile_id]);
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to fetch enrollments' });
  }
});

// POST /api/enrollments
router.post('/', requireAuth, async (req, res) => {
  if (req.user.role !== 'student') {
    return res.status(403).json({ error: 'Only students can enroll' });
  }

  const { course_id } = req.body;
  if (!course_id) {
    return res.status(400).json({ error: 'course_id is required' });
  }

  try {
    const result = await pool.query(`
      INSERT INTO enrollments (student_id, course_id)
      VALUES ($1, $2)
      ON CONFLICT (student_id, course_id) DO NOTHING
      RETURNING *
    `, [req.user.profile_id, course_id]);

    if (result.rows.length === 0) {
      return res.status(409).json({ error: 'Already enrolled in this course' });
    }

    res.status(201).json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to enroll' });
  }
});

// DELETE /api/enrollments/:course_id
router.delete('/:course_id', requireAuth, async (req, res) => {
  if (req.user.role !== 'student') {
    return res.status(403).json({ error: 'Forbidden' });
  }

  try {
    const result = await pool.query(`
      UPDATE enrollments SET status = 'cancelled'
      WHERE student_id = $1 AND course_id = $2 AND status = 'active'
      RETURNING *
    `, [req.user.profile_id, req.params.course_id]);

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Enrollment not found' });
    }
    res.json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to cancel enrollment' });
  }
});

// GET /api/enrollments/course/:courseId
router.get('/course/:courseId', requireAuth, async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT e.*, u.full_name, stu.id as student_id
      FROM enrollments e
      JOIN students stu ON stu.id = e.student_id
      JOIN users u ON u.id = stu.user_id
      WHERE e.course_id = $1 AND e.status = 'active'
      ORDER BY e.enrolled_at DESC
    `, [req.params.courseId]);
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to fetch course enrollments' });
  }
});

module.exports = router;