const express = require('express');
const pool = require('../db');

const router = express.Router();

// Auth middleware helper
const requireAuth = (req, res, next) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'No token provided' });
  }
  try {
    const jwt = require('jsonwebtoken');
    const token = authHeader.split(' ')[1];
    req.user = jwt.verify(token, process.env.JWT_SECRET || 'super_secret_jwt_key_2026');
    next();
  } catch {
    return res.status(401).json({ error: 'Invalid token' });
  }
};

// GET /api/courses
router.get('/', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT c.*, u.full_name as instructor_name,
             (SELECT COUNT(*) FROM enrollments WHERE course_id = c.id) as student_count,
             (SELECT COUNT(*) FROM modules WHERE course_id = c.id) as module_count
      FROM courses c
      JOIN instructors i ON i.id = c.instructor_id
      JOIN users u ON u.id = i.user_id
      WHERE c.is_published = TRUE
      ORDER BY c.created_at DESC
    `);
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to fetch courses' });
  }
});

// GET /api/courses/:id
router.get('/:id', async (req, res) => {
  try {
    const course = await pool.query(`
      SELECT c.*, u.full_name as instructor_name, i.bio, i.expertise
      FROM courses c
      JOIN instructors i ON i.id = c.instructor_id
      JOIN users u ON u.id = i.user_id
      WHERE c.id = $1
    `, [req.params.id]);

    if (course.rows.length === 0) {
      return res.status(404).json({ error: 'Course not found' });
    }

    const modules = await pool.query(`
      SELECT m.*,
             (SELECT COUNT(*) FROM assignments WHERE module_id = m.id) as assignment_count
      FROM modules m
      WHERE m.course_id = $1
      ORDER BY m.order_index
    `, [req.params.id]);

    res.json({
      ...course.rows[0],
      modules: modules.rows,
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to fetch course' });
  }
});

// GET /api/courses/instructor/mine
router.get('/instructor/mine', requireAuth, async (req, res) => {
  if (req.user.role !== 'instructor') {
    return res.status(403).json({ error: 'Forbidden' });
  }

  try {
    const result = await pool.query(`
      SELECT c.*,
             (SELECT COUNT(*) FROM enrollments WHERE course_id = c.id) as student_count,
             (SELECT COUNT(*) FROM modules WHERE course_id = c.id) as module_count,
             (SELECT SUM(p.amount) FROM payments p WHERE p.course_id = c.id AND p.status = 'completed') as total_revenue
      FROM courses c
      WHERE c.instructor_id = $1
      ORDER BY c.created_at DESC
    `, [req.user.profile_id]);
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to fetch instructor courses' });
  }
});

// POST /api/courses
router.post('/', requireAuth, async (req, res) => {
  if (req.user.role !== 'instructor') {
    return res.status(403).json({ error: 'Forbidden' });
  }

  const { title, description, price, category } = req.body;
  if (!title) {
    return res.status(400).json({ error: 'Title is required' });
  }

  try {
    const result = await pool.query(`
      INSERT INTO courses (instructor_id, title, description, price, category, is_published, thumbnail)
      VALUES ($1, $2, $3, $4, $5, FALSE, $6)
      RETURNING *
    `, [req.user.profile_id, title, description || '', price || 0, category || '', '']);

    res.status(201).json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to create course' });
  }
});

// PUT /api/courses/:id
router.put('/:id', requireAuth, async (req, res) => {
  const { title, description, price, category, is_published, thumbnail } = req.body;

  try {
    // Verify ownership
    const owner = await pool.query('SELECT instructor_id FROM courses WHERE id = $1', [req.params.id]);
    if (owner.rows.length === 0) return res.status(404).json({ error: 'Course not found' });
    if (owner.rows[0].instructor_id !== req.user.profile_id) {
      return res.status(403).json({ error: 'Forbidden' });
    }

    const result = await pool.query(`
      UPDATE courses SET
        title = COALESCE($1, title),
        description = COALESCE($2, description),
        price = COALESCE($3, price),
        category = COALESCE($4, category),
        is_published = COALESCE($5, is_published),
        thumbnail = COALESCE($6, thumbnail),
        updated_at = CURRENT_TIMESTAMP
      WHERE id = $7
      RETURNING *
    `, [title, description, price, category, is_published, thumbnail, req.params.id]);

    res.json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to update course' });
  }
});

// DELETE /api/courses/:id
router.delete('/:id', requireAuth, async (req, res) => {
  try {
    const owner = await pool.query('SELECT instructor_id FROM courses WHERE id = $1', [req.params.id]);
    if (owner.rows.length === 0) return res.status(404).json({ error: 'Course not found' });
    if (owner.rows[0].instructor_id !== req.user.profile_id) {
      return res.status(403).json({ error: 'Forbidden' });
    }

    await pool.query('DELETE FROM courses WHERE id = $1', [req.params.id]);
    res.json({ message: 'Course deleted' });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to delete course' });
  }
});

module.exports = router;
module.exports.requireAuth = requireAuth;