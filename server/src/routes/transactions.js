// src/routes/transactions.js
// Transaction (order/top-up history) routes

const express = require('express');
const router = express.Router();
const { v4: uuidv4 } = require('uuid');
const db = require('../database/db');
const { authenticateToken, requireAdmin } = require('../middleware/auth');

const VALID_PAYMENT_METHODS = ['Dana', 'GoPay', 'OVO', 'QRIS', 'Bank Transfer'];
const VALID_STATUSES = ['pending', 'processing', 'success', 'failed'];

function formatTx(tx) {
  return {
    ...tx,
    price: Number(tx.price),
    amount: Number(tx.amount),
    bonus: Number(tx.bonus),
  };
}

// ─────────────────────────────────────────────
// GET /api/transactions
// Returns paginated transaction history for the logged-in user
// Admin can see all: ?all=true
// Filter: ?status=success&page=1&limit=20
// ─────────────────────────────────────────────
router.get('/', authenticateToken, (req, res) => {
  const { status, page = 1, limit = 20, all } = req.query;
  const offset = (parseInt(page) - 1) * parseInt(limit);
  const isAdmin = req.user.role === 'admin';

  let query = `
    SELECT
      t.*,
      g.name  as game_name,
      g.slug  as game_slug,
      g.icon_url as game_icon_url,
      g.gradient_start, g.gradient_end,
      p.label as package_label,
      u.name  as user_name,
      u.email as user_email
    FROM transactions t
    JOIN games    g ON t.game_id    = g.id
    JOIN packages p ON t.package_id = p.id
    JOIN users    u ON t.user_id    = u.id
    WHERE 1=1
  `;
  const params = [];

  // Non-admin users only see their own transactions
  if (!isAdmin || all !== 'true') {
    query += ' AND t.user_id = ?';
    params.push(req.user.id);
  }

  if (status && VALID_STATUSES.includes(status)) {
    query += ' AND t.status = ?';
    params.push(status);
  }

  query += ' ORDER BY t.created_at DESC LIMIT ? OFFSET ?';
  params.push(parseInt(limit), offset);

  const transactions = db.prepare(query).all(...params);

  // Count query
  let countQuery = `
    SELECT COUNT(*) as cnt FROM transactions t WHERE 1=1
    ${(!isAdmin || all !== 'true') ? 'AND t.user_id = ?' : ''}
    ${status && VALID_STATUSES.includes(status) ? 'AND t.status = ?' : ''}
  `;
  const countParams = [];
  if (!isAdmin || all !== 'true') countParams.push(req.user.id);
  if (status && VALID_STATUSES.includes(status)) countParams.push(status);
  const total = db.prepare(countQuery).get(...countParams).cnt;

  res.json({
    success: true,
    data: transactions.map(formatTx),
    pagination: { page: parseInt(page), limit: parseInt(limit), total },
  });
});

// ─────────────────────────────────────────────
// GET /api/transactions/:id
// ─────────────────────────────────────────────
router.get('/:id', authenticateToken, (req, res) => {
  const tx = db.prepare(`
    SELECT
      t.*,
      g.name  as game_name,
      g.slug  as game_slug,
      g.icon_url as game_icon_url,
      g.gradient_start, g.gradient_end,
      g.requires_zone_id,
      p.label as package_label,
      u.name  as user_name,
      u.email as user_email,
      u.phone as user_phone
    FROM transactions t
    JOIN games    g ON t.game_id    = g.id
    JOIN packages p ON t.package_id = p.id
    JOIN users    u ON t.user_id    = u.id
    WHERE t.id = ?
  `).get(req.params.id);

  if (!tx) {
    return res.status(404).json({ success: false, message: 'Transaksi tidak ditemukan' });
  }

  // Users can only access their own transactions
  if (req.user.role !== 'admin' && tx.user_id !== req.user.id) {
    return res.status(403).json({ success: false, message: 'Akses ditolak' });
  }

  res.json({ success: true, data: formatTx(tx) });
});

// ─────────────────────────────────────────────
// POST /api/transactions
// Create a new top-up order
// ─────────────────────────────────────────────
router.post('/', authenticateToken, (req, res) => {
  const { game_id, package_id, game_user_id, game_zone_id, payment_method } = req.body;

  // Validate required fields
  if (!game_id || !package_id || !game_user_id || !payment_method) {
    return res.status(400).json({
      success: false,
      message: 'game_id, package_id, game_user_id, dan payment_method wajib diisi',
    });
  }

  if (!VALID_PAYMENT_METHODS.includes(payment_method)) {
    return res.status(400).json({
      success: false,
      message: `Metode pembayaran tidak valid. Pilih: ${VALID_PAYMENT_METHODS.join(', ')}`,
    });
  }

  // Get game and validate zone_id requirement
  const game = db.prepare('SELECT * FROM games WHERE id = ? AND is_active = 1').get(game_id);
  if (!game) {
    return res.status(404).json({ success: false, message: 'Game tidak ditemukan' });
  }

  if (game.requires_zone_id && !game_zone_id) {
    return res.status(400).json({
      success: false,
      message: `Game ${game.name} memerlukan Zone ID`,
    });
  }

  // Get package
  const pkg = db.prepare(
    'SELECT * FROM packages WHERE id = ? AND game_id = ? AND is_active = 1'
  ).get(package_id, game_id);
  if (!pkg) {
    return res.status(404).json({ success: false, message: 'Package tidak tersedia' });
  }

  // Create transaction
  const txId = uuidv4();
  db.prepare(`
    INSERT INTO transactions
      (id, user_id, game_id, package_id, game_user_id, game_zone_id,
       payment_method, amount, bonus, price, status)
    VALUES
      (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'pending')
  `).run(
    txId, req.user.id, game_id, package_id,
    game_user_id, game_zone_id || null,
    payment_method, pkg.amount, pkg.bonus, pkg.price
  );

  // Simulate async processing: set to 'success' after creation
  // In production, you'd integrate with a payment gateway here
  db.prepare("UPDATE transactions SET status='success', updated_at=datetime('now') WHERE id=?").run(txId);

  const tx = db.prepare(`
    SELECT t.*, g.name as game_name, p.label as package_label
    FROM transactions t
    JOIN games g ON t.game_id = g.id
    JOIN packages p ON t.package_id = p.id
    WHERE t.id = ?
  `).get(txId);

  res.status(201).json({
    success: true,
    message: 'Transaksi berhasil dibuat',
    data: formatTx(tx),
  });
});

// ─────────────────────────────────────────────
// PATCH /api/transactions/:id/status  [Admin]
// Update transaction status
// ─────────────────────────────────────────────
router.patch('/:id/status', authenticateToken, requireAdmin, (req, res) => {
  const { status, notes } = req.body;

  if (!status || !VALID_STATUSES.includes(status)) {
    return res.status(400).json({
      success: false,
      message: `Status tidak valid. Pilih: ${VALID_STATUSES.join(', ')}`,
    });
  }

  const tx = db.prepare('SELECT id FROM transactions WHERE id = ?').get(req.params.id);
  if (!tx) {
    return res.status(404).json({ success: false, message: 'Transaksi tidak ditemukan' });
  }

  db.prepare(`
    UPDATE transactions
    SET status=?, notes=?, updated_at=datetime('now')
    WHERE id=?
  `).run(status, notes || null, req.params.id);

  const updated = db.prepare('SELECT * FROM transactions WHERE id = ?').get(req.params.id);
  res.json({ success: true, data: formatTx(updated) });
});

// ─────────────────────────────────────────────
// GET /api/transactions/stats/summary  [Auth]
// User's transaction stats
// ─────────────────────────────────────────────
router.get('/stats/summary', authenticateToken, (req, res) => {
  const isAdmin = req.user.role === 'admin';
  const userId = req.user.id;

  const whereClause = isAdmin ? '' : 'WHERE user_id = ?';
  const params = isAdmin ? [] : [userId];

  const totalSpent = db.prepare(
    `SELECT COALESCE(SUM(price), 0) as total FROM transactions ${whereClause} ${whereClause ? 'AND' : 'WHERE'} status='success'`
  ).get(...(isAdmin ? [] : [userId])).total;

  const counts = db.prepare(
    `SELECT status, COUNT(*) as count FROM transactions ${whereClause} GROUP BY status`
  ).all(...params);

  const recentGames = db.prepare(`
    SELECT DISTINCT g.name, g.slug, t.game_id
    FROM transactions t
    JOIN games g ON t.game_id = g.id
    ${whereClause}
    ORDER BY t.created_at DESC
    LIMIT 5
  `).all(...params);

  res.json({
    success: true,
    data: {
      total_spent: Number(totalSpent),
      counts: Object.fromEntries(counts.map(c => [c.status, c.count])),
      recent_games: recentGames,
    },
  });
});

module.exports = router;
