const express = require('express');
const pool = require('../db');
const { requireAuth } = require('./courses');

const router = express.Router();

// GET /api/modules/course/:courseId
router.get('/course/:courseId', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT m.*,
             (SELECT COUNT(*) FROM assignments WHERE module_id = m.id) as assignment_count,
             (SELECT json_agg(json_build_object(
               'id', a.id, 'title', a.title, 'description', a.description,
               'due_date', a.due_date, 'max_score', a.max_score
             ) ORDER BY a.created_at)
              FROM assignments a WHERE a.module_id = m.id) as assignments
      FROM modules m
      WHERE m.course_id = $1
      ORDER BY m.order_index
    `, [req.params.courseId]);
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to fetch modules' });
  }
});

// GET /api/modules/:id
router.get('/:id', async (req, res) => {
  try {
    const module = await pool.query('SELECT * FROM modules WHERE id = $1', [req.params.id]);
    if (!module.rows.length) return res.status(404).json({ error: 'Module not found' });

    const assignments = await pool.query(`
      SELECT a.*,
             (SELECT COUNT(*) FROM submissions s WHERE s.assignment_id = a.id) as submission_count
      FROM assignments a WHERE a.module_id = $1 ORDER BY a.created_at
    `, [req.params.id]);

    res.json({ ...module.rows[0], assignments: assignments.rows });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to fetch module' });
  }
});

// POST /api/modules
router.post('/', requireAuth, async (req, res) => {
  if (req.user.role !== 'instructor') {
    return res.status(403).json({ error: 'Forbidden' });
  }

  const { course_id, title, description } = req.body;
  if (!course_id || !title) {
    return res.status(400).json({ error: 'course_id and title are required' });
  }

  try {
    // Verify ownership
    const owner = await pool.query('SELECT instructor_id FROM courses WHERE id = $1', [course_id]);
    if (!owner.rows.length || owner.rows[0].instructor_id !== req.user.profile_id) {
      return res.status(403).json({ error: 'Forbidden' });
    }

    const maxOrder = await pool.query(
      'SELECT COALESCE(MAX(order_index), -1) + 1 as next_order FROM modules WHERE course_id = $1',
      [course_id]
    );

    const result = await pool.query(
      'INSERT INTO modules (course_id, title, description, order_index) VALUES ($1, $2, $3, $4) RETURNING *',
      [course_id, title, description || '', maxOrder.rows[0].next_order]
    );

    res.status(201).json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to create module' });
  }
});

// PUT /api/modules/:id
router.put('/:id', requireAuth, async (req, res) => {
  const { title, description, order_index } = req.body;

  try {
    const module = await pool.query(`
      SELECT m.*, c.instructor_id
      FROM modules m JOIN courses c ON c.id = m.course_id
      WHERE m.id = $1
    `, [req.params.id]);

    if (!module.rows.length) return res.status(404).json({ error: 'Module not found' });
    if (module.rows[0].instructor_id !== req.user.profile_id) {
      return res.status(403).json({ error: 'Forbidden' });
    }

    const result = await pool.query(`
      UPDATE modules SET
        title = COALESCE($1, title),
        description = COALESCE($2, description),
        order_index = COALESCE($3, order_index)
      WHERE id = $4
      RETURNING *
    `, [title, description, order_index, req.params.id]);

    res.json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to update module' });
  }
});

// DELETE /api/modules/:id
router.delete('/:id', requireAuth, async (req, res) => {
  try {
    const module = await pool.query(`
      SELECT m.*, c.instructor_id
      FROM modules m JOIN courses c ON c.id = m.course_id
      WHERE m.id = $1
    `, [req.params.id]);

    if (!module.rows.length) return res.status(404).json({ error: 'Module not found' });
    if (module.rows[0].instructor_id !== req.user.profile_id) {
      return res.status(403).json({ error: 'Forbidden' });
    }

    await pool.query('DELETE FROM modules WHERE id = $1', [req.params.id]);
    res.json({ message: 'Module deleted' });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to delete module' });
  }
});

module.exports = router;