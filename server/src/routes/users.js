// src/routes/users.js
// User account management routes

const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const path = require('path');
const db = require('../database/db');
const { authenticateToken, requireAdmin } = require('../middleware/auth');
const upload = require('../middleware/upload');

function buildImageUrl(req, relPath) {
  if (!relPath) return null;
  if (relPath.startsWith('http') || relPath.startsWith('@drawable/')) return relPath;
  return `${req.protocol}://${req.get('host')}/uploads/${relPath}`;
}

function safeUser(user, req) {
  const { password, ...rest } = user;
  return { ...rest, avatar_url: buildImageUrl(req, rest.avatar_url) };
}

// ─────────────────────────────────────────────
// GET /api/users/me
// Get current user's profile
// ─────────────────────────────────────────────
router.get('/me', authenticateToken, (req, res) => {
  const user = db.prepare(
    'SELECT * FROM users WHERE id = ? AND is_active = 1'
  ).get(req.user.id);

  if (!user) {
    return res.status(404).json({ success: false, message: 'User tidak ditemukan' });
  }
  res.json({ success: true, data: safeUser(user, req) });
});

// ─────────────────────────────────────────────
// PUT /api/users/me
// Update current user's profile (name, phone)
// ─────────────────────────────────────────────
router.put('/me', authenticateToken, (req, res) => {
  const { name, phone } = req.body;

  const user = db.prepare('SELECT * FROM users WHERE id = ?').get(req.user.id);
  if (!user) {
    return res.status(404).json({ success: false, message: 'User tidak ditemukan' });
  }

  // Check phone uniqueness
  if (phone && phone !== user.phone) {
    const existing = db.prepare('SELECT id FROM users WHERE phone = ? AND id != ?').get(phone, req.user.id);
    if (existing) {
      return res.status(409).json({ success: false, message: 'Nomor HP sudah digunakan' });
    }
  }

  db.prepare(`
    UPDATE users SET
      name  = ?,
      phone = ?,
      updated_at = datetime('now')
    WHERE id = ?
  `).run(name ?? user.name, phone ?? user.phone, req.user.id);

  const updated = db.prepare('SELECT * FROM users WHERE id = ?').get(req.user.id);
  res.json({ success: true, data: safeUser(updated, req) });
});

// ─────────────────────────────────────────────
// PUT /api/users/me/avatar
// Upload/update current user's avatar
// ─────────────────────────────────────────────
router.put('/me/avatar', authenticateToken, upload.single('avatar'), (req, res) => {
  if (!req.file) {
    return res.status(400).json({ success: false, message: 'File avatar wajib diupload' });
  }

  const avatarPath = `avatars/${req.file.filename}`;
  db.prepare("UPDATE users SET avatar_url=?, updated_at=datetime('now') WHERE id=?").run(avatarPath, req.user.id);

  const user = db.prepare('SELECT * FROM users WHERE id = ?').get(req.user.id);
  res.json({
    success: true,
    message: 'Avatar berhasil diupdate',
    data: safeUser(user, req),
  });
});

// ─────────────────────────────────────────────
// PUT /api/users/me/password
// Change password
// ─────────────────────────────────────────────
router.put('/me/password', authenticateToken, (req, res) => {
  const { current_password, new_password } = req.body;

  if (!current_password || !new_password) {
    return res.status(400).json({ success: false, message: 'Password lama dan baru wajib diisi' });
  }
  if (new_password.length < 6) {
    return res.status(400).json({ success: false, message: 'Password baru minimal 6 karakter' });
  }

  const user = db.prepare('SELECT * FROM users WHERE id = ?').get(req.user.id);
  const isValid = bcrypt.compareSync(current_password, user.password);
  if (!isValid) {
    return res.status(401).json({ success: false, message: 'Password lama tidak sesuai' });
  }

  const hashed = bcrypt.hashSync(new_password, 10);
  db.prepare("UPDATE users SET password=?, updated_at=datetime('now') WHERE id=?").run(hashed, req.user.id);

  // Invalidate all refresh tokens
  db.prepare('DELETE FROM refresh_tokens WHERE user_id = ?').run(req.user.id);

  res.json({ success: true, message: 'Password berhasil diubah. Silakan login kembali.' });
});

// ─────────────────────────────────────────────
// Admin only: list all users
// GET /api/users?page=1&limit=20&search=
// ─────────────────────────────────────────────
router.get('/', authenticateToken, requireAdmin, (req, res) => {
  const { page = 1, limit = 20, search } = req.query;
  const offset = (parseInt(page) - 1) * parseInt(limit);

  let query = 'SELECT * FROM users WHERE 1=1';
  const params = [];

  if (search) {
    query += ' AND (name LIKE ? OR email LIKE ? OR phone LIKE ?)';
    const like = `%${search}%`;
    params.push(like, like, like);
  }

  query += ' ORDER BY created_at DESC LIMIT ? OFFSET ?';
  params.push(parseInt(limit), offset);

  const users = db.prepare(query).all(...params);

  const countQuery = `SELECT COUNT(*) as cnt FROM users WHERE 1=1 ${search ? 'AND (name LIKE ? OR email LIKE ? OR phone LIKE ?)' : ''}`;
  const countParams = search ? [`%${search}%`, `%${search}%`, `%${search}%`] : [];
  const total = db.prepare(countQuery).get(...countParams).cnt;

  res.json({
    success: true,
    data: users.map(u => safeUser(u, req)),
    pagination: { page: parseInt(page), limit: parseInt(limit), total },
  });
});

// ─────────────────────────────────────────────
// Admin: get specific user
// GET /api/users/:id
// ─────────────────────────────────────────────
router.get('/:id', authenticateToken, requireAdmin, (req, res) => {
  const user = db.prepare('SELECT * FROM users WHERE id = ?').get(req.params.id);
  if (!user) {
    return res.status(404).json({ success: false, message: 'User tidak ditemukan' });
  }
  res.json({ success: true, data: safeUser(user, req) });
});

// ─────────────────────────────────────────────
// Admin: deactivate user
// DELETE /api/users/:id
// ─────────────────────────────────────────────
router.delete('/:id', authenticateToken, requireAdmin, (req, res) => {
  const user = db.prepare('SELECT id FROM users WHERE id = ?').get(req.params.id);
  if (!user) {
    return res.status(404).json({ success: false, message: 'User tidak ditemukan' });
  }
  db.prepare("UPDATE users SET is_active=0, updated_at=datetime('now') WHERE id=?").run(req.params.id);
  db.prepare('DELETE FROM refresh_tokens WHERE user_id = ?').run(req.params.id);
  res.json({ success: true, message: 'User berhasil dinonaktifkan' });
});

module.exports = router;
