// src/routes/games.js
// Game & category routes

const express = require('express');
const router = express.Router();
const path = require('path');
const db = require('../database/db');
const { authenticateToken, requireAdmin } = require('../middleware/auth');
const upload = require('../middleware/upload');

// ─────────────────────────────────────────────
// Helper: build full image URL
// ─────────────────────────────────────────────
function buildImageUrl(req, relPath) {
  if (!relPath) return null;
  if (relPath.startsWith('http') || relPath.startsWith('@drawable/')) return relPath;
  return `${req.protocol}://${req.get('host')}/uploads/${relPath}`;
}

function formatGame(req, game) {
  return {
    ...game,
    is_popular: !!game.is_popular,
    is_new: !!game.is_new,
    is_active: !!game.is_active,
    requires_zone_id: !!game.requires_zone_id,
    icon_url: buildImageUrl(req, game.icon_url),
    banner_url: buildImageUrl(req, game.banner_url),
  };
}

// ─────────────────────────────────────────────
// GET /api/games
// Query: ?category=moba&popular=true&search=legend&page=1&limit=20
// ─────────────────────────────────────────────
router.get('/', (req, res) => {
  const { category, popular, search, page = 1, limit = 50 } = req.query;
  const offset = (parseInt(page) - 1) * parseInt(limit);

  let query = `
    SELECT g.*, c.name as category_name, c.slug as category_slug
    FROM games g
    JOIN categories c ON g.category_id = c.id
    WHERE g.is_active = 1
  `;
  const params = [];

  if (category && category !== 'all') {
    query += ' AND c.slug = ?';
    params.push(category);
  }
  if (popular === 'true') {
    query += ' AND g.is_popular = 1';
  }
  if (search) {
    query += ' AND (g.name LIKE ? OR c.name LIKE ?)';
    params.push(`%${search}%`, `%${search}%`);
  }

  query += ' ORDER BY g.sort_order ASC, g.name ASC LIMIT ? OFFSET ?';
  params.push(parseInt(limit), offset);

  const games = db.prepare(query).all(...params);
  const total = db.prepare(`
    SELECT COUNT(*) as cnt FROM games g
    JOIN categories c ON g.category_id = c.id
    WHERE g.is_active = 1 ${category && category !== 'all' ? 'AND c.slug = ?' : ''}
  `).get(...(category && category !== 'all' ? [category] : [])).cnt;

  res.json({
    success: true,
    data: games.map(g => formatGame(req, g)),
    pagination: { page: parseInt(page), limit: parseInt(limit), total },
  });
});

// ─────────────────────────────────────────────
// GET /api/games/:id
// ─────────────────────────────────────────────
router.get('/:id', (req, res) => {
  const game = db.prepare(`
    SELECT g.*, c.name as category_name, c.slug as category_slug
    FROM games g
    JOIN categories c ON g.category_id = c.id
    WHERE g.id = ? AND g.is_active = 1
  `).get(req.params.id);

  if (!game) {
    return res.status(404).json({ success: false, message: 'Game tidak ditemukan' });
  }

  // Include packages
  const packages = db.prepare(
    'SELECT * FROM packages WHERE game_id = ? AND is_active = 1 ORDER BY sort_order ASC'
  ).all(game.id);

  res.json({
    success: true,
    data: {
      ...formatGame(req, game),
      packages: packages.map(p => ({ ...p, is_popular: !!p.is_popular, is_active: !!p.is_active })),
    },
  });
});

// ─────────────────────────────────────────────
// GET /api/games/:id/packages
// ─────────────────────────────────────────────
router.get('/:id/packages', (req, res) => {
  const game = db.prepare('SELECT id, name FROM games WHERE id = ? AND is_active = 1').get(req.params.id);
  if (!game) {
    return res.status(404).json({ success: false, message: 'Game tidak ditemukan' });
  }

  const packages = db.prepare(
    'SELECT * FROM packages WHERE game_id = ? AND is_active = 1 ORDER BY sort_order ASC'
  ).all(game.id);

  res.json({
    success: true,
    data: packages.map(p => ({ ...p, is_popular: !!p.is_popular })),
  });
});

// ─────────────────────────────────────────────
// POST /api/games  [Admin]
// ─────────────────────────────────────────────
router.post('/', authenticateToken, requireAdmin, upload.fields([
  { name: 'icon', maxCount: 1 },
  { name: 'banner', maxCount: 1 },
]), (req, res) => {
  const {
    name, slug, category_id, description,
    gradient_start, gradient_end, accent_color,
    is_popular, is_new, requires_zone_id, sort_order,
  } = req.body;

  if (!name || !slug || !category_id) {
    return res.status(400).json({ success: false, message: 'name, slug, dan category_id wajib diisi' });
  }

  const icon_url = req.files?.icon ? `icons/${req.files.icon[0].filename}` : null;
  const banner_url = req.files?.banner ? `banners/${req.files.banner[0].filename}` : null;

  try {
    const result = db.prepare(`
      INSERT INTO games
        (name, slug, category_id, description, icon_url, banner_url,
         gradient_start, gradient_end, accent_color,
         is_popular, is_new, requires_zone_id, sort_order)
      VALUES
        (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `).run(
      name, slug, category_id, description || null, icon_url, banner_url,
      gradient_start || null, gradient_end || null, accent_color || null,
      is_popular ? 1 : 0, is_new ? 1 : 0, requires_zone_id ? 1 : 0,
      parseInt(sort_order) || 0
    );

    const game = db.prepare('SELECT * FROM games WHERE id = ?').get(result.lastInsertRowid);
    res.status(201).json({ success: true, data: formatGame(req, game) });
  } catch (err) {
    if (err.message.includes('UNIQUE')) {
      return res.status(409).json({ success: false, message: 'Slug sudah digunakan' });
    }
    throw err;
  }
});

// ─────────────────────────────────────────────
// PUT /api/games/:id  [Admin]
// ─────────────────────────────────────────────
router.put('/:id', authenticateToken, requireAdmin, upload.fields([
  { name: 'icon', maxCount: 1 },
  { name: 'banner', maxCount: 1 },
]), (req, res) => {
  const game = db.prepare('SELECT * FROM games WHERE id = ?').get(req.params.id);
  if (!game) {
    return res.status(404).json({ success: false, message: 'Game tidak ditemukan' });
  }

  const fields = {
    name: req.body.name ?? game.name,
    slug: req.body.slug ?? game.slug,
    category_id: req.body.category_id ?? game.category_id,
    description: req.body.description ?? game.description,
    gradient_start: req.body.gradient_start ?? game.gradient_start,
    gradient_end: req.body.gradient_end ?? game.gradient_end,
    accent_color: req.body.accent_color ?? game.accent_color,
    is_popular: req.body.is_popular !== undefined ? (req.body.is_popular ? 1 : 0) : game.is_popular,
    is_new: req.body.is_new !== undefined ? (req.body.is_new ? 1 : 0) : game.is_new,
    requires_zone_id: req.body.requires_zone_id !== undefined ? (req.body.requires_zone_id ? 1 : 0) : game.requires_zone_id,
    is_active: req.body.is_active !== undefined ? (req.body.is_active ? 1 : 0) : game.is_active,
    sort_order: req.body.sort_order !== undefined ? parseInt(req.body.sort_order) : game.sort_order,
    icon_url: req.files?.icon ? `icons/${req.files.icon[0].filename}` : game.icon_url,
    banner_url: req.files?.banner ? `banners/${req.files.banner[0].filename}` : game.banner_url,
  };

  db.prepare(`
    UPDATE games SET
      name=?, slug=?, category_id=?, description=?,
      icon_url=?, banner_url=?, gradient_start=?, gradient_end=?, accent_color=?,
      is_popular=?, is_new=?, requires_zone_id=?, is_active=?, sort_order=?,
      updated_at=datetime('now')
    WHERE id=?
  `).run(
    fields.name, fields.slug, fields.category_id, fields.description,
    fields.icon_url, fields.banner_url, fields.gradient_start, fields.gradient_end, fields.accent_color,
    fields.is_popular, fields.is_new, fields.requires_zone_id, fields.is_active, fields.sort_order,
    req.params.id
  );

  const updated = db.prepare('SELECT * FROM games WHERE id = ?').get(req.params.id);
  res.json({ success: true, data: formatGame(req, updated) });
});

// ─────────────────────────────────────────────
// DELETE /api/games/:id  [Admin]
// ─────────────────────────────────────────────
router.delete('/:id', authenticateToken, requireAdmin, (req, res) => {
  const game = db.prepare('SELECT id FROM games WHERE id = ?').get(req.params.id);
  if (!game) {
    return res.status(404).json({ success: false, message: 'Game tidak ditemukan' });
  }
  db.prepare("UPDATE games SET is_active=0, updated_at=datetime('now') WHERE id=?").run(req.params.id);
  res.json({ success: true, message: 'Game berhasil dihapus' });
});

// ─────────────────────────────────────────────
// GET /api/games/categories/all
// ─────────────────────────────────────────────
router.get('/categories/all', (req, res) => {
  const categories = db.prepare('SELECT * FROM categories ORDER BY sort_order ASC').all();
  res.json({ success: true, data: categories });
});

module.exports = router;
