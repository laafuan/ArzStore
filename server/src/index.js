// src/index.js
// Main Express server entry point for ArzStore API

const express = require('express');
const cors = require('cors');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;

// ─────────────────────────────────────────────
// Middleware
// ─────────────────────────────────────────────
app.use(cors({
  origin: '*', // In production, restrict to your app's domain/origin
  methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'],
  allowedHeaders: ['Content-Type', 'Authorization'],
}));

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Serve uploaded images as static files
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));

// ─────────────────────────────────────────────
// Routes
// ─────────────────────────────────────────────
const authRouter         = require('./routes/auth');
const usersRouter        = require('./routes/users');
const gamesRouter        = require('./routes/games');
const packagesRouter     = require('./routes/packages');
const bannersRouter      = require('./routes/banners');
const categoriesRouter   = require('./routes/categories');
const transactionsRouter = require('./routes/transactions');

app.use('/api/auth',         authRouter);
app.use('/api/users',        usersRouter);
app.use('/api/games',        gamesRouter);
app.use('/api/packages',     packagesRouter);
app.use('/api/banners',      bannersRouter);
app.use('/api/categories',   categoriesRouter);
app.use('/api/transactions', transactionsRouter);

// ─────────────────────────────────────────────
// Health Check
// ─────────────────────────────────────────────
app.get('/api/health', (req, res) => {
  res.json({
    success: true,
    message: 'ArzStore API is running 🚀',
    version: '1.0.0',
    timestamp: new Date().toISOString(),
  });
});

// ─────────────────────────────────────────────
// API docs overview
// ─────────────────────────────────────────────
app.get('/api', (req, res) => {
  res.json({
    name: 'ArzStore API',
    version: '1.0.0',
    endpoints: {
      auth:         '/api/auth',
      users:        '/api/users',
      games:        '/api/games',
      packages:     '/api/packages',
      banners:      '/api/banners',
      categories:   '/api/categories',
      transactions: '/api/transactions',
      health:       '/api/health',
    },
  });
});

// ─────────────────────────────────────────────
// 404 handler
// ─────────────────────────────────────────────
app.use((req, res) => {
  res.status(404).json({ success: false, message: `Route ${req.method} ${req.path} tidak ditemukan` });
});

// ─────────────────────────────────────────────
// Global error handler
// ─────────────────────────────────────────────
app.use((err, req, res, next) => {
  console.error('❌ Error:', err.message);

  if (err.code === 'LIMIT_FILE_SIZE') {
    return res.status(400).json({ success: false, message: 'Ukuran file terlalu besar (max 5MB)' });
  }

  res.status(500).json({
    success: false,
    message: process.env.NODE_ENV === 'production' ? 'Internal server error' : err.message,
  });
});

// ─────────────────────────────────────────────
// Start server
// ─────────────────────────────────────────────
app.listen(PORT, () => {
  console.log(`\n🚀 ArzStore API Server running on http://localhost:${PORT}`);
  console.log(`📖 API docs: http://localhost:${PORT}/api`);
  console.log(`❤️  Health:  http://localhost:${PORT}/api/health\n`);
});

module.exports = app;
