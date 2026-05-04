// src/routes/categories.js
// Game category management routes

const express = require('express');
const router = express.Router();
const db = require('../database/db');
const { authenticateToken, requireAdmin } = require('../middleware/auth');
const upload = require('../middleware/upload');

function buildImageUrl(req, relPath) {
  if (!relPath) return null;
  if (relPath.startsWith('http') || relPath.startsWith('@drawable/')) return relPath;
  return `${req.protocol}://${req.get('host')}/uploads/${relPath}`;
}

// ─────────────────────────────────────────────
// GET /api/categories
// ─────────────────────────────────────────────
router.get('/', (req, res) => {
  const categories = db.prepare('SELECT * FROM categories ORDER BY sort_order ASC').all();

  // Include game count per category
  const withCount = categories.map(cat => {
    const gameCount = db.prepare(
      'SELECT COUNT(*) as cnt FROM games WHERE category_id = ? AND is_active = 1'
    ).get(cat.id).cnt;

    return {
      ...cat,
      game_count: gameCount,
      icon_url: buildImageUrl(req, cat.icon_url),
    };
  });

  res.json({ success: true, data: withCount });
});

// ─────────────────────────────────────────────
// GET /api/categories/:slug
// ─────────────────────────────────────────────
router.get('/:slug', (req, res) => {
  const category = db.prepare('SELECT * FROM categories WHERE slug = ?').get(req.params.slug);
  if (!category) {
    return res.status(404).json({ success: false, message: 'Kategori tidak ditemukan' });
  }

  const games = db.prepare(
    'SELECT * FROM games WHERE category_id = ? AND is_active = 1 ORDER BY sort_order ASC, name ASC'
  ).all(category.id);

  res.json({
    success: true,
    data: {
      ...category,
      icon_url: buildImageUrl(req, category.icon_url),
      games,
    },
  });
});

// ─────────────────────────────────────────────
// POST /api/categories  [Admin]
// ─────────────────────────────────────────────
router.post('/', authenticateToken, requireAdmin, upload.single('icon'), (req, res) => {
  const { name, slug, sort_order } = req.body;

  if (!name || !slug) {
    return res.status(400).json({ success: false, message: 'name dan slug wajib diisi' });
  }

  const icon_url = req.file ? `icons/${req.file.filename}` : null;

  try {
    const result = db.prepare(
      'INSERT INTO categories (name, slug, icon_url, sort_order) VALUES (?, ?, ?, ?)'
    ).run(name, slug, icon_url, parseInt(sort_order) || 0);

    const category = db.prepare('SELECT * FROM categories WHERE id = ?').get(result.lastInsertRowid);
    res.status(201).json({
      success: true,
      data: { ...category, icon_url: buildImageUrl(req, category.icon_url) },
    });
  } catch (err) {
    if (err.message.includes('UNIQUE')) {
      return res.status(409).json({ success: false, message: 'Slug atau nama sudah digunakan' });
    }
    throw err;
  }
});

// ─────────────────────────────────────────────
// PUT /api/categories/:id  [Admin]
// ─────────────────────────────────────────────
router.put('/:id', authenticateToken, requireAdmin, upload.single('icon'), (req, res) => {
  const category = db.prepare('SELECT * FROM categories WHERE id = ?').get(req.params.id);
  if (!category) {
    return res.status(404).json({ success: false, message: 'Kategori tidak ditemukan' });
  }

  const name       = req.body.name ?? category.name;
  const slug       = req.body.slug ?? category.slug;
  const sort_order = req.body.sort_order !== undefined ? parseInt(req.body.sort_order) : category.sort_order;
  const icon_url   = req.file ? `icons/${req.file.filename}` : category.icon_url;

  db.prepare('UPDATE categories SET name=?, slug=?, icon_url=?, sort_order=? WHERE id=?').run(
    name, slug, icon_url, sort_order, req.params.id
  );

  const updated = db.prepare('SELECT * FROM categories WHERE id = ?').get(req.params.id);
  res.json({
    success: true,
    data: { ...updated, icon_url: buildImageUrl(req, updated.icon_url) },
  });
});

// ─────────────────────────────────────────────
// DELETE /api/categories/:id  [Admin]
// ─────────────────────────────────────────────
router.delete('/:id', authenticateToken, requireAdmin, (req, res) => {
  const category = db.prepare('SELECT id FROM categories WHERE id = ?').get(req.params.id);
  if (!category) {
    return res.status(404).json({ success: false, message: 'Kategori tidak ditemukan' });
  }
  const gamesInCat = db.prepare('SELECT COUNT(*) as cnt FROM games WHERE category_id = ?').get(req.params.id).cnt;
  if (gamesInCat > 0) {
    return res.status(409).json({ success: false, message: 'Tidak bisa hapus kategori yang masih ada gamenya' });
  }
  db.prepare('DELETE FROM categories WHERE id = ?').run(req.params.id);
  res.json({ success: true, message: 'Kategori berhasil dihapus' });
});

module.exports = router;
