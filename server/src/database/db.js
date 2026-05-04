// src/database/db.js
// Database connection using better-sqlite3 (synchronous, file-based)

const { DatabaseSync } = require('node:sqlite');
const path = require('path');
const fs = require('fs');

// Ensure data directory exists
const dataDir = path.join(__dirname, '../../data');
if (!fs.existsSync(dataDir)) {
  fs.mkdirSync(dataDir, { recursive: true });
}

const dbPath = path.join(dataDir, 'arzstore.db');
const db = new DatabaseSync(dbPath);

// Transaction helper for node:sqlite
db.transaction = function(fn) {
  return function(...args) {
    db.exec('BEGIN EXCLUSIVE');
    try {
      const result = fn(...args);
      db.exec('COMMIT');
      return result;
    } catch (err) {
      db.exec('ROLLBACK');
      throw err;
    }
  };
};

// Enable WAL mode for better performance
db.exec('PRAGMA journal_mode = WAL');
db.exec('PRAGMA foreign_keys = ON');

/**
 * Initialize all database tables
 */
function initializeDatabase() {
  db.exec(`
    -- =====================
    --  USERS / ACCOUNTS
    -- =====================
    CREATE TABLE IF NOT EXISTS users (
      id          TEXT PRIMARY KEY,
      name        TEXT NOT NULL,
      email       TEXT UNIQUE NOT NULL,
      phone       TEXT UNIQUE NOT NULL,
      password    TEXT NOT NULL,
      avatar_url  TEXT,
      role        TEXT NOT NULL DEFAULT 'user',    -- 'user' | 'admin'
      is_active   INTEGER NOT NULL DEFAULT 1,
      created_at  TEXT NOT NULL DEFAULT (datetime('now')),
      updated_at  TEXT NOT NULL DEFAULT (datetime('now'))
    );

    -- =====================
    --  GAME CATEGORIES
    -- =====================
    CREATE TABLE IF NOT EXISTS categories (
      id          INTEGER PRIMARY KEY AUTOINCREMENT,
      name        TEXT NOT NULL UNIQUE,
      slug        TEXT NOT NULL UNIQUE,
      icon_url    TEXT,
      sort_order  INTEGER NOT NULL DEFAULT 0
    );

    -- =====================
    --  GAMES
    -- =====================
    CREATE TABLE IF NOT EXISTS games (
      id              INTEGER PRIMARY KEY AUTOINCREMENT,
      name            TEXT NOT NULL,
      slug            TEXT NOT NULL UNIQUE,
      category_id     INTEGER NOT NULL REFERENCES categories(id),
      description     TEXT,
      icon_url        TEXT,           -- path/URL to game icon image
      banner_url      TEXT,           -- path/URL to game banner image
      gradient_start  TEXT,           -- hex color e.g. "#1E3A8A"
      gradient_end    TEXT,
      accent_color    TEXT,
      is_popular      INTEGER NOT NULL DEFAULT 0,
      is_new          INTEGER NOT NULL DEFAULT 0,
      requires_zone_id INTEGER NOT NULL DEFAULT 0,
      is_active       INTEGER NOT NULL DEFAULT 1,
      sort_order      INTEGER NOT NULL DEFAULT 0,
      created_at      TEXT NOT NULL DEFAULT (datetime('now')),
      updated_at      TEXT NOT NULL DEFAULT (datetime('now'))
    );

    -- =====================
    --  TOP-UP PACKAGES
    -- =====================
    CREATE TABLE IF NOT EXISTS packages (
      id          INTEGER PRIMARY KEY AUTOINCREMENT,
      game_id     INTEGER NOT NULL REFERENCES games(id) ON DELETE CASCADE,
      label       TEXT NOT NULL,      -- "86 Diamonds"
      amount      INTEGER NOT NULL,   -- numeric amount (86)
      bonus       INTEGER NOT NULL DEFAULT 0,
      price       INTEGER NOT NULL,   -- price in IDR (Rupiah)
      is_popular  INTEGER NOT NULL DEFAULT 0,
      is_active   INTEGER NOT NULL DEFAULT 1,
      sort_order  INTEGER NOT NULL DEFAULT 0
    );

    -- =====================
    --  BANNERS
    -- =====================
    CREATE TABLE IF NOT EXISTS banners (
      id              INTEGER PRIMARY KEY AUTOINCREMENT,
      game_id         INTEGER REFERENCES games(id) ON DELETE SET NULL,
      title           TEXT NOT NULL,
      subtitle        TEXT,
      discount_text   TEXT,           -- e.g. "BONUS 20%"
      image_url       TEXT,           -- banner image
      gradient_start  TEXT,
      gradient_end    TEXT,
      accent_color    TEXT,
      link_url        TEXT,           -- deep-link or external URL
      is_active       INTEGER NOT NULL DEFAULT 1,
      sort_order      INTEGER NOT NULL DEFAULT 0,
      created_at      TEXT NOT NULL DEFAULT (datetime('now'))
    );

    -- =====================
    --  TRANSACTIONS
    -- =====================
    CREATE TABLE IF NOT EXISTS transactions (
      id              TEXT PRIMARY KEY,   -- UUID
      user_id         TEXT NOT NULL REFERENCES users(id),
      game_id         INTEGER NOT NULL REFERENCES games(id),
      package_id      INTEGER NOT NULL REFERENCES packages(id),
      game_user_id    TEXT NOT NULL,      -- in-game user ID
      game_zone_id    TEXT,               -- zone/server ID (ML etc.)
      payment_method  TEXT NOT NULL,      -- "Dana" | "GoPay" | "OVO" | "QRIS" | "Bank Transfer"
      amount          INTEGER NOT NULL,   -- package amount
      bonus           INTEGER NOT NULL DEFAULT 0,
      price           INTEGER NOT NULL,   -- price paid in IDR
      status          TEXT NOT NULL DEFAULT 'pending', -- 'pending' | 'processing' | 'success' | 'failed'
      notes           TEXT,
      created_at      TEXT NOT NULL DEFAULT (datetime('now')),
      updated_at      TEXT NOT NULL DEFAULT (datetime('now'))
    );

    -- =====================
    --  REFRESH TOKENS
    -- =====================
    CREATE TABLE IF NOT EXISTS refresh_tokens (
      id          INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id     TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      token       TEXT NOT NULL UNIQUE,
      expires_at  TEXT NOT NULL,
      created_at  TEXT NOT NULL DEFAULT (datetime('now'))
    );

    -- Indexes
    CREATE INDEX IF NOT EXISTS idx_transactions_user_id   ON transactions(user_id);
    CREATE INDEX IF NOT EXISTS idx_transactions_status    ON transactions(status);
    CREATE INDEX IF NOT EXISTS idx_packages_game_id       ON packages(game_id);
    CREATE INDEX IF NOT EXISTS idx_games_category         ON games(category_id);
    CREATE INDEX IF NOT EXISTS idx_games_popular          ON games(is_popular);
  `);

  console.log('✅ Database tables initialized');
}

initializeDatabase();

module.exports = db;
