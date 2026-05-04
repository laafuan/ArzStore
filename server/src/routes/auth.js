// src/routes/auth.js
// Authentication routes: register, login, refresh token, profile

const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const { v4: uuidv4 } = require('uuid');
const db = require('../database/db');
const { generateTokens, authenticateToken, JWT_SECRET } = require('../middleware/auth');
const jwt = require('jsonwebtoken');

// ─────────────────────────────────────────────
// POST /api/auth/register
// ─────────────────────────────────────────────
router.post('/register', (req, res) => {
  const { name, email, phone, password } = req.body;

  if (!name || !email || !phone || !password) {
    return res.status(400).json({ success: false, message: 'Semua field wajib diisi' });
  }

  if (password.length < 6) {
    return res.status(400).json({ success: false, message: 'Password minimal 6 karakter' });
  }

  // Check duplicate
  const existing = db.prepare('SELECT id FROM users WHERE email = ? OR phone = ?').get(email, phone);
  if (existing) {
    return res.status(409).json({ success: false, message: 'Email atau nomor HP sudah terdaftar' });
  }

  const hashedPassword = bcrypt.hashSync(password, 10);
  const id = uuidv4();

  db.prepare(`
    INSERT INTO users (id, name, email, phone, password, role)
    VALUES (?, ?, ?, ?, ?, 'user')
  `).run(id, name, email, phone, hashedPassword);

  const user = db.prepare('SELECT id, name, email, phone, avatar_url, role, created_at FROM users WHERE id = ?').get(id);
  const { accessToken, refreshToken } = generateTokens(user);

  // Store refresh token
  const expiresAt = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString();
  db.prepare('INSERT INTO refresh_tokens (user_id, token, expires_at) VALUES (?, ?, ?)').run(id, refreshToken, expiresAt);

  res.status(201).json({
    success: true,
    message: 'Registrasi berhasil',
    data: { user, accessToken, refreshToken },
  });
});

// ─────────────────────────────────────────────
// POST /api/auth/login
// ─────────────────────────────────────────────
router.post('/login', (req, res) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(400).json({ success: false, message: 'Email dan password wajib diisi' });
  }

  const user = db.prepare('SELECT * FROM users WHERE email = ?').get(email);
  if (!user || !user.is_active) {
    return res.status(401).json({ success: false, message: 'Email atau password salah' });
  }

  const isValid = bcrypt.compareSync(password, user.password);
  if (!isValid) {
    return res.status(401).json({ success: false, message: 'Email atau password salah' });
  }

  const { accessToken, refreshToken } = generateTokens(user);

  // Store refresh token
  const expiresAt = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString();
  db.prepare('INSERT INTO refresh_tokens (user_id, token, expires_at) VALUES (?, ?, ?)').run(user.id, refreshToken, expiresAt);

  const { password: _, ...safeUser } = user;
  res.json({
    success: true,
    message: 'Login berhasil',
    data: { user: safeUser, accessToken, refreshToken },
  });
});

// ─────────────────────────────────────────────
// POST /api/auth/refresh
// ─────────────────────────────────────────────
router.post('/refresh', (req, res) => {
  const { refreshToken } = req.body;
  if (!refreshToken) {
    return res.status(400).json({ success: false, message: 'Refresh token diperlukan' });
  }

  try {
    const decoded = jwt.verify(refreshToken, JWT_SECRET + '-refresh');

    // Check if token exists in DB
    const tokenRow = db.prepare(
      "SELECT * FROM refresh_tokens WHERE token = ? AND user_id = ? AND expires_at > datetime('now')"
    ).get(refreshToken, decoded.id);

    if (!tokenRow) {
      return res.status(403).json({ success: false, message: 'Refresh token tidak valid' });
    }

    const user = db.prepare('SELECT * FROM users WHERE id = ?').get(decoded.id);
    const { accessToken, refreshToken: newRefreshToken } = generateTokens(user);

    // Rotate refresh token
    const expiresAt = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString();
    db.prepare('DELETE FROM refresh_tokens WHERE token = ?').run(refreshToken);
    db.prepare('INSERT INTO refresh_tokens (user_id, token, expires_at) VALUES (?, ?, ?)').run(user.id, newRefreshToken, expiresAt);

    res.json({ success: true, data: { accessToken, refreshToken: newRefreshToken } });
  } catch {
    res.status(403).json({ success: false, message: 'Refresh token tidak valid atau sudah kadaluarsa' });
  }
});

// ─────────────────────────────────────────────
// POST /api/auth/logout
// ─────────────────────────────────────────────
router.post('/logout', authenticateToken, (req, res) => {
  const { refreshToken } = req.body;
  if (refreshToken) {
    db.prepare('DELETE FROM refresh_tokens WHERE token = ?').run(refreshToken);
  }
  res.json({ success: true, message: 'Logout berhasil' });
});

// ─────────────────────────────────────────────
// GET /api/auth/me
// ─────────────────────────────────────────────
router.get('/me', authenticateToken, (req, res) => {
  const user = db.prepare(
    'SELECT id, name, email, phone, avatar_url, role, created_at, updated_at FROM users WHERE id = ?'
  ).get(req.user.id);

  if (!user) {
    return res.status(404).json({ success: false, message: 'User tidak ditemukan' });
  }

  res.json({ success: true, data: user });
});

module.exports = router;
