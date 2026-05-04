// src/routes/packages.js
// Top-up package management routes (admin CRUD)

const express = require('express');
const router = express.Router();
const db = require('../database/db');
const { authenticateToken, requireAdmin } = require('../middleware/auth');

// ─────────────────────────────────────────────
// GET /api/packages?game_id=1
// ─────────────────────────────────────────────
router.get('/', (req, res) => {
  const { game_id } = req.query;

  let query = `
    SELECT p.*, g.name as game_name, g.slug as game_slug
    FROM packages p
    JOIN games g ON p.game_id = g.id
    WHERE p.is_active = 1
  `;
  const params = [];

  if (game_id) {
    query += ' AND p.game_id = ?';
    params.push(game_id);
  }
  query += ' ORDER BY p.sort_order ASC, p.price ASC';

  const packages = db.prepare(query).all(...params);
  res.json({
    success: true,
    data: packages.map(p => ({ ...p, is_popular: !!p.is_popular, is_active: !!p.is_active })),
  });
});

// ─────────────────────────────────────────────
// GET /api/packages/:id
// ─────────────────────────────────────────────
router.get('/:id', (req, res) => {
  const pkg = db.prepare(`
    SELECT p.*, g.name as game_name, g.slug as game_slug
    FROM packages p
    JOIN games g ON p.game_id = g.id
    WHERE p.id = ?
  `).get(req.params.id);

  if (!pkg) {
    return res.status(404).json({ success: false, message: 'Package tidak ditemukan' });
  }
  res.json({ success: true, data: { ...pkg, is_popular: !!pkg.is_popular } });
});

// ─────────────────────────────────────────────
// POST /api/packages  [Admin]
// ─────────────────────────────────────────────
router.post('/', authenticateToken, requireAdmin, (req, res) => {
  const { game_id, label, amount, bonus, price, is_popular, sort_order } = req.body;

  if (!game_id || !label || !amount || !price) {
    return res.status(400).json({ success: false, message: 'game_id, label, amount, dan price wajib diisi' });
  }

  const game = db.prepare('SELECT id FROM games WHERE id = ?').get(game_id);
  if (!game) {
    return res.status(404).json({ success: false, message: 'Game tidak ditemukan' });
  }

  const result = db.prepare(`
    INSERT INTO packages (game_id, label, amount, bonus, price, is_popular, sort_order)
    VALUES (?, ?, ?, ?, ?, ?, ?)
  `).run(
    game_id, label, parseInt(amount), parseInt(bonus) || 0,
    parseInt(price), is_popular ? 1 : 0, parseInt(sort_order) || 0
  );

  const pkg = db.prepare('SELECT * FROM packages WHERE id = ?').get(result.lastInsertRowid);
  res.status(201).json({ success: true, data: { ...pkg, is_popular: !!pkg.is_popular } });
});

// ─────────────────────────────────────────────
// PUT /api/packages/:id  [Admin]
// ─────────────────────────────────────────────
router.put('/:id', authenticateToken, requireAdmin, (req, res) => {
  const pkg = db.prepare('SELECT * FROM packages WHERE id = ?').get(req.params.id);
  if (!pkg) {
    return res.status(404).json({ success: false, message: 'Package tidak ditemukan' });
  }

  const fields = {
    label:      req.body.label ?? pkg.label,
    amount:     req.body.amount !== undefined ? parseInt(req.body.amount) : pkg.amount,
    bonus:      req.body.bonus !== undefined ? parseInt(req.body.bonus) : pkg.bonus,
    price:      req.body.price !== undefined ? parseInt(req.body.price) : pkg.price,
    is_popular: req.body.is_popular !== undefined ? (req.body.is_popular ? 1 : 0) : pkg.is_popular,
    is_active:  req.body.is_active !== undefined ? (req.body.is_active ? 1 : 0) : pkg.is_active,
    sort_order: req.body.sort_order !== undefined ? parseInt(req.body.sort_order) : pkg.sort_order,
  };

  db.prepare(`
    UPDATE packages SET
      label=?, amount=?, bonus=?, price=?, is_popular=?, is_active=?, sort_order=?
    WHERE id=?
  `).run(
    fields.label, fields.amount, fields.bonus, fields.price,
    fields.is_popular, fields.is_active, fields.sort_order,
    req.params.id
  );

  const updated = db.prepare('SELECT * FROM packages WHERE id = ?').get(req.params.id);
  res.json({ success: true, data: { ...updated, is_popular: !!updated.is_popular } });
});

// ─────────────────────────────────────────────
// DELETE /api/packages/:id  [Admin]
// ─────────────────────────────────────────────
router.delete('/:id', authenticateToken, requireAdmin, (req, res) => {
  const pkg = db.prepare('SELECT id FROM packages WHERE id = ?').get(req.params.id);
  if (!pkg) {
    return res.status(404).json({ success: false, message: 'Package tidak ditemukan' });
  }
  db.prepare('UPDATE packages SET is_active=0 WHERE id=?').run(req.params.id);
  res.json({ success: true, message: 'Package berhasil dihapus' });
});

module.exports = router;
