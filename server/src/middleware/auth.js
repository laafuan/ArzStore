// src/middleware/auth.js
// JWT authentication middleware

const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || 'arzstore-secret-key-change-in-production';

/**
 * Verify JWT token and attach user to request
 */
function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1]; // "Bearer <token>"

  if (!token) {
    return res.status(401).json({ success: false, message: 'Token tidak ditemukan' });
  }

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.user = decoded;
    next();
  } catch (err) {
    return res.status(403).json({ success: false, message: 'Token tidak valid atau sudah kadaluarsa' });
  }
}

/**
 * Require admin role
 */
function requireAdmin(req, res, next) {
  if (!req.user || req.user.role !== 'admin') {
    return res.status(403).json({ success: false, message: 'Akses ditolak: hanya admin' });
  }
  next();
}

/**
 * Generate access & refresh tokens
 */
function generateTokens(user) {
  const payload = { id: user.id, email: user.email, role: user.role };

  const accessToken = jwt.sign(payload, JWT_SECRET, { expiresIn: '7d' });

  const refreshToken = jwt.sign(
    { id: user.id },
    JWT_SECRET + '-refresh',
    { expiresIn: '30d' }
  );

  return { accessToken, refreshToken };
}

module.exports = { authenticateToken, requireAdmin, generateTokens, JWT_SECRET };
