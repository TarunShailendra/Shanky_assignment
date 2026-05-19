const express = require('express');
const pool = require('../db');
const { requireAuth } = require('./courses');

const router = express.Router();

// GET /api/assignments/module/:moduleId
router.get('/module/:moduleId', async (req, res) => {
  try {
    const result = await pool.query(
      'SELECT * FROM assignments WHERE module_id = $1 ORDER BY created_at',
      [req.params.moduleId]
    );
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to fetch assignments' });
  }
});

// GET /api/assignments/:id
router.get('/:id', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT a.*, m.title as module_title, m.course_id, c.title as course_title
      FROM assignments a
      JOIN modules m ON m.id = a.module_id
      JOIN courses c ON c.id = m.course_id
      WHERE a.id = $1
    `, [req.params.id]);

    if (!result.rows.length) return res.status(404).json({ error: 'Assignment not found' });
    res.json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to fetch assignment' });
  }
});

// POST /api/assignments
router.post('/', requireAuth, async (req, res) => {
  if (req.user.role !== 'instructor') {
    return res.status(403).json({ error: 'Forbidden' });
  }

  const { module_id, title, description, due_date, max_score } = req.body;
  if (!module_id || !title) {
    return res.status(400).json({ error: 'module_id and title are required' });
  }

  try {
    const owner = await pool.query(`
      SELECT c.instructor_id FROM modules m JOIN courses c ON c.id = m.course_id
      WHERE m.id = $1
    `, [module_id]);

    if (!owner.rows.length || owner.rows[0].instructor_id !== req.user.profile_id) {
      return res.status(403).json({ error: 'Forbidden' });
    }

    const result = await pool.query(`
      INSERT INTO assignments (module_id, title, description, due_date, max_score)
      VALUES ($1, $2, $3, $4, $5)
      RETURNING *
    `, [module_id, title, description || '', due_date || null, max_score || 100]);

    res.status(201).json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to create assignment' });
  }
});

// PUT /api/assignments/:id
router.put('/:id', requireAuth, async (req, res) => {
  const { title, description, due_date, max_score } = req.body;

  try {
    const owner = await pool.query(`
      SELECT c.instructor_id FROM assignments a
      JOIN modules m ON m.id = a.module_id
      JOIN courses c ON c.id = m.course_id
      WHERE a.id = $1
    `, [req.params.id]);

    if (!owner.rows.length) return res.status(404).json({ error: 'Assignment not found' });
    if (owner.rows[0].instructor_id !== req.user.profile_id) {
      return res.status(403).json({ error: 'Forbidden' });
    }

    const result = await pool.query(`
      UPDATE assignments SET
        title = COALESCE($1, title),
        description = COALESCE($2, description),
        due_date = COALESCE($3, due_date),
        max_score = COALESCE($4, max_score)
      WHERE id = $5
      RETURNING *
    `, [title, description, due_date, max_score, req.params.id]);

    res.json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to update assignment' });
  }
});

// DELETE /api/assignments/:id
router.delete('/:id', requireAuth, async (req, res) => {
  try {
    const owner = await pool.query(`
      SELECT c.instructor_id FROM assignments a
      JOIN modules m ON m.id = a.module_id
      JOIN courses c ON c.id = m.course_id
      WHERE a.id = $1
    `, [req.params.id]);

    if (!owner.rows.length) return res.status(404).json({ error: 'Assignment not found' });
    if (owner.rows[0].instructor_id !== req.user.profile_id) {
      return res.status(403).json({ error: 'Forbidden' });
    }

    await pool.query('DELETE FROM assignments WHERE id = $1', [req.params.id]);
    res.json({ message: 'Assignment deleted' });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to delete assignment' });
  }
});

module.exports = router;