const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const pool = require('../db');

const router = express.Router();
const JWT_SECRET = process.env.JWT_SECRET || 'super_secret_jwt_key_2026';
const JWT_EXPIRES_IN = '7d';

const signToken = (user) => {
  return jwt.sign(
    { id: user.id, email: user.email, role: user.role },
    JWT_SECRET,
    { expiresIn: JWT_EXPIRES_IN }
  );
};

// POST /api/auth/register
router.post('/register', async (req, res) => {
  const { email, password, full_name, role = 'student' } = req.body;

  if (!email || !password || !full_name) {
    return res.status(400).json({ error: 'Email, password, and full name are required' });
  }

  if (!['student', 'instructor'].includes(role)) {
    return res.status(400).json({ error: 'Role must be student or instructor' });
  }

  try {
    // Check if email already exists
    const existing = await pool.query('SELECT id FROM users WHERE email = $1', [email]);
    if (existing.rows.length > 0) {
      return res.status(409).json({ error: 'Email already registered' });
    }

    const password_hash = await bcrypt.hash(password, 10);

    const client = await pool.connect();
    try {
      await client.query('BEGIN');

      const userResult = await client.query(
        'INSERT INTO users (email, password_hash, full_name, role) VALUES ($1, $2, $3, $4) RETURNING id, email, full_name, role',
        [email, password_hash, full_name, role]
      );
      const user = userResult.rows[0];

      if (role === 'instructor') {
        await client.query('INSERT INTO instructors (user_id) VALUES ($1)', [user.id]);
      } else {
        await client.query('INSERT INTO students (user_id) VALUES ($1)', [user.id]);
      }

      await client.query('COMMIT');

      const token = signToken(user);
      res.status(201).json({ token, user });
    } catch (err) {
      await client.query('ROLLBACK');
      throw err;
    } finally {
      client.release();
    }
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Registration failed' });
  }
});

// POST /api/auth/login
router.post('/login', async (req, res) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(400).json({ error: 'Email and password are required' });
  }

  try {
    const result = await pool.query(
      'SELECT id, email, password_hash, full_name, role FROM users WHERE email = $1',
      [email]
    );

    if (result.rows.length === 0) {
      return res.status(401).json({ error: 'Invalid email or password' });
    }

    const user = result.rows[0];
    const valid = await bcrypt.compare(password, user.password_hash);

    if (!valid) {
      return res.status(401).json({ error: 'Invalid email or password' });
    }

    // Attach profile id
    if (user.role === 'instructor') {
      const inv = await pool.query('SELECT id FROM instructors WHERE user_id = $1', [user.id]);
      user.profile_id = inv.rows[0]?.id;
    } else if (user.role === 'student') {
      const stu = await pool.query('SELECT id FROM students WHERE user_id = $1', [user.id]);
      user.profile_id = stu.rows[0]?.id;
    }

    delete user.password_hash;
    const token = signToken(user);
    res.json({ token, user });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Login failed' });
  }
});

// GET /api/auth/me
router.get('/me', async (req, res) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'No token provided' });
  }

  try {
    const token = authHeader.split(' ')[1];
    const decoded = jwt.verify(token, JWT_SECRET);

    const result = await pool.query(
      'SELECT id, email, full_name, role, created_at FROM users WHERE id = $1',
      [decoded.id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'User not found' });
    }

    const user = result.rows[0];
    if (user.role === 'instructor') {
      const inv = await pool.query('SELECT id, bio, expertise FROM instructors WHERE user_id = $1', [user.id]);
      user.instructor = inv.rows[0];
    } else if (user.role === 'student') {
      const stu = await pool.query('SELECT id, date_of_birth, phone, address FROM students WHERE user_id = $1', [user.id]);
      user.student = stu.rows[0];
    }

    res.json(user);
  } catch (err) {
    if (err.name === 'JsonWebTokenError') {
      return res.status(401).json({ error: 'Invalid token' });
    }
    console.error(err);
    res.status(500).json({ error: 'Failed to authenticate' });
  }
});

module.exports = router;