// src/routes/banners.js
// Banner management routes

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

function formatBanner(req, banner) {
  return {
    ...banner,
    is_active: !!banner.is_active,
    image_url: buildImageUrl(req, banner.image_url),
  };
}

// ─────────────────────────────────────────────
// GET /api/banners
// Returns active banners ordered by sort_order
// ─────────────────────────────────────────────
router.get('/', (req, res) => {
  const banners = db.prepare(`
    SELECT b.*, g.name as game_name, g.slug as game_slug
    FROM banners b
    LEFT JOIN games g ON b.game_id = g.id
    WHERE b.is_active = 1
    ORDER BY b.sort_order ASC
  `).all();

  res.json({
    success: true,
    data: banners.map(b => formatBanner(req, b)),
  });
});

// ─────────────────────────────────────────────
// GET /api/banners/:id
// ─────────────────────────────────────────────
router.get('/:id', (req, res) => {
  const banner = db.prepare(`
    SELECT b.*, g.name as game_name, g.slug as game_slug
    FROM banners b
    LEFT JOIN games g ON b.game_id = g.id
    WHERE b.id = ?
  `).get(req.params.id);

  if (!banner) {
    return res.status(404).json({ success: false, message: 'Banner tidak ditemukan' });
  }

  res.json({ success: true, data: formatBanner(req, banner) });
});

// ─────────────────────────────────────────────
// POST /api/banners  [Admin]
// ─────────────────────────────────────────────
router.post('/', authenticateToken, requireAdmin, upload.single('image'), (req, res) => {
  const {
    game_id, title, subtitle, discount_text,
    gradient_start, gradient_end, accent_color,
    link_url, sort_order,
  } = req.body;

  if (!title) {
    return res.status(400).json({ success: false, message: 'Title banner wajib diisi' });
  }

  const image_url = req.file ? `banners/${req.file.filename}` : null;

  const result = db.prepare(`
    INSERT INTO banners
      (game_id, title, subtitle, discount_text, image_url,
       gradient_start, gradient_end, accent_color, link_url, sort_order)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
  `).run(
    game_id || null, title, subtitle || null, discount_text || null, image_url,
    gradient_start || null, gradient_end || null, accent_color || null,
    link_url || null, parseInt(sort_order) || 0
  );

  const banner = db.prepare('SELECT * FROM banners WHERE id = ?').get(result.lastInsertRowid);
  res.status(201).json({ success: true, data: formatBanner(req, banner) });
});

// ─────────────────────────────────────────────
// PUT /api/banners/:id  [Admin]
// ─────────────────────────────────────────────
router.put('/:id', authenticateToken, requireAdmin, upload.single('image'), (req, res) => {
  const banner = db.prepare('SELECT * FROM banners WHERE id = ?').get(req.params.id);
  if (!banner) {
    return res.status(404).json({ success: false, message: 'Banner tidak ditemukan' });
  }

  const fields = {
    game_id:        req.body.game_id ?? banner.game_id,
    title:          req.body.title ?? banner.title,
    subtitle:       req.body.subtitle ?? banner.subtitle,
    discount_text:  req.body.discount_text ?? banner.discount_text,
    image_url:      req.file ? `banners/${req.file.filename}` : banner.image_url,
    gradient_start: req.body.gradient_start ?? banner.gradient_start,
    gradient_end:   req.body.gradient_end ?? banner.gradient_end,
    accent_color:   req.body.accent_color ?? banner.accent_color,
    link_url:       req.body.link_url ?? banner.link_url,
    is_active:      req.body.is_active !== undefined ? (req.body.is_active ? 1 : 0) : banner.is_active,
    sort_order:     req.body.sort_order !== undefined ? parseInt(req.body.sort_order) : banner.sort_order,
  };

  db.prepare(`
    UPDATE banners SET
      game_id=?, title=?, subtitle=?, discount_text=?, image_url=?,
      gradient_start=?, gradient_end=?, accent_color=?,
      link_url=?, is_active=?, sort_order=?
    WHERE id=?
  `).run(
    fields.game_id, fields.title, fields.subtitle, fields.discount_text, fields.image_url,
    fields.gradient_start, fields.gradient_end, fields.accent_color,
    fields.link_url, fields.is_active, fields.sort_order,
    req.params.id
  );

  const updated = db.prepare('SELECT * FROM banners WHERE id = ?').get(req.params.id);
  res.json({ success: true, data: formatBanner(req, updated) });
});

// ─────────────────────────────────────────────
// DELETE /api/banners/:id  [Admin]
// ─────────────────────────────────────────────
router.delete('/:id', authenticateToken, requireAdmin, (req, res) => {
  const banner = db.prepare('SELECT id FROM banners WHERE id = ?').get(req.params.id);
  if (!banner) {
    return res.status(404).json({ success: false, message: 'Banner tidak ditemukan' });
  }
  db.prepare('UPDATE banners SET is_active=0 WHERE id=?').run(req.params.id);
  res.json({ success: true, message: 'Banner berhasil dihapus' });
});

module.exports = router;
