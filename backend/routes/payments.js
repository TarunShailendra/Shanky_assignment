const express = require('express');
const pool = require('../db');
const { requireAuth } = require('./courses');

const router = express.Router();

// GET /api/payments/student — payments for the current student
router.get('/student', requireAuth, async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT p.*, c.title as course_title, c.thumbnail as course_thumbnail
      FROM payments p
      JOIN courses c ON c.id = p.course_id
      WHERE p.student_id = $1
      ORDER BY p.paid_at DESC
    `, [req.user.profile_id]);
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to fetch payments' });
  }
});

// GET /api/payments/course/:courseId — payments for a course (instructor)
router.get('/course/:courseId', requireAuth, async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT p.*, u.full_name as student_name, st.id as student_internal_id
      FROM payments p
      JOIN students st ON st.id = p.student_id
      JOIN users u ON u.id = st.user_id
      WHERE p.course_id = $1 AND p.status = 'completed'
      ORDER BY p.paid_at DESC
    `, [req.params.courseId]);
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to fetch course payments' });
  }
});

// POST /api/payments — process a payment
router.post('/', requireAuth, async (req, res) => {
  if (req.user.role !== 'student') {
    return res.status(403).json({ error: 'Only students can make payments' });
  }

  const { course_id, amount, payment_method = 'card' } = req.body;
  if (!course_id || !amount) {
    return res.status(400).json({ error: 'course_id and amount are required' });
  }

  try {
    // Check if already paid
    const existing = await pool.query(`
      SELECT id FROM payments
      WHERE student_id = $1 AND course_id = $2 AND status = 'completed'
    `, [req.user.profile_id, course_id]);

    if (existing.rows.length > 0) {
      return res.status(409).json({ error: 'Already paid for this course' });
    }

    const transaction_id = `txn_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const result = await pool.query(`
      INSERT INTO payments (student_id, course_id, amount, payment_method, status, transaction_id)
      VALUES ($1, $2, $3, $4, 'completed', $5)
      RETURNING *
    `, [req.user.profile_id, course_id, amount, payment_method, transaction_id]);

    // Auto-enroll on successful payment
    if (result.rows[0].status === 'completed') {
      await pool.query(`
        INSERT INTO enrollments (student_id, course_id)
        VALUES ($1, $2)
        ON CONFLICT (student_id, course_id) DO NOTHING
      `, [req.user.profile_id, course_id]);
    }

    res.status(201).json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Payment failed' });
  }
});

// GET /api/payments/instructor — all payments across instructor's courses
router.get('/instructor', requireAuth, async (req, res) => {
  if (req.user.role !== 'instructor') {
    return res.status(403).json({ error: 'Forbidden' });
  }

  try {
    const result = await pool.query(`
      SELECT p.*, c.title as course_title, u.full_name as student_name
      FROM payments p
      JOIN courses c ON c.id = p.course_id
      JOIN students st ON st.id = p.student_id
      JOIN users u ON u.id = st.user_id
      WHERE c.instructor_id = $1 AND p.status = 'completed'
      ORDER BY p.paid_at DESC
    `, [req.user.profile_id]);
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to fetch payments' });
  }
});

// GET /api/payments/summary — payment summary stats
router.get('/summary', requireAuth, async (req, res) => {
  try {
    let result;
    if (req.user.role === 'instructor') {
      result = await pool.query(`
        SELECT
          COALESCE(SUM(p.amount), 0) as total_revenue,
          COUNT(p.id) as total_transactions,
          COUNT(DISTINCT p.course_id) as courses_sold,
          COUNT(DISTINCT p.student_id) as total_students
        FROM payments p
        JOIN courses c ON c.id = p.course_id
        WHERE c.instructor_id = $1 AND p.status = 'completed'
      `, [req.user.profile_id]);
    } else {
      result = await pool.query(`
        SELECT
          COALESCE(SUM(amount), 0) as total_spent,
          COUNT(id) as total_payments,
          COUNT(DISTINCT course_id) as courses_purchased
        FROM payments
        WHERE student_id = $1 AND status = 'completed'
      `, [req.user.profile_id]);
    }
    res.json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to fetch payment summary' });
  }
});

module.exports = router;