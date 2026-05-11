// src/database/seed.js
// Seed the database with initial data matching the Android SampleData.kt

const db = require('./db');
const bcrypt = require('bcryptjs');
const { v4: uuidv4 } = require('uuid');

console.log('🌱 Seeding database...');

// ====================
// CATEGORIES
// ====================
const insertCategory = db.prepare(`
  INSERT OR IGNORE INTO categories (name, slug, sort_order)
  VALUES (@name, @slug, @sort_order)
`);

const categories = [
  { name: 'MOBA',          slug: 'moba',          sort_order: 1 },
  { name: 'Battle Royale', slug: 'battle-royale',  sort_order: 2 },
  { name: 'FPS',           slug: 'fps',            sort_order: 3 },
  { name: 'RPG',           slug: 'rpg',            sort_order: 4 },
  { name: 'Strategy',      slug: 'strategy',       sort_order: 5 },
  { name: 'SPORT',         slug: 'sport',          sort_order: 6 },
  { name: 'PLATFORM',      slug: 'platform',       sort_order: 7 },
];

const seedCategories = db.transaction(() => {
  for (const cat of categories) insertCategory.run(cat);
});
seedCategories();
console.log('✅ Categories seeded');

// Helper: get category id by slug
const getCategoryId = db.prepare('SELECT id FROM categories WHERE slug = ?');

// ====================
// GAMES
// ====================
const insertGame = db.prepare(`
  INSERT OR IGNORE INTO games
    (name, slug, category_id, description, icon_url, banner_url, gradient_start, gradient_end,
     accent_color, is_popular, is_new, requires_zone_id, sort_order)
  VALUES
    (@name, @slug, @category_id, @description, @icon_url, @banner_url, @gradient_start, @gradient_end,
     @accent_color, @is_popular, @is_new, @requires_zone_id, @sort_order)
`);

const games = [
  {
    name: 'Mobile Legends', slug: 'mobile-legends', category_slug: 'moba',
    description: 'Game MOBA mobile paling populer di Asia Tenggara. Top up Diamond ML dengan mudah dan cepat.',
    icon_url: '@drawable/logo_ml', banner_url: '@drawable/banner_ml',
    gradient_start: '#1E3A8A', gradient_end: '#3B82F6', accent_color: '#60A5FA',
    is_popular: 1, is_new: 0, requires_zone_id: 1, sort_order: 1,
  },
  {
    name: 'Free Fire', slug: 'free-fire', category_slug: 'battle-royale',
    description: 'Game battle royale mobile hit dari Garena. Top up Diamond FF dengan harga terbaik.',
    icon_url: '@drawable/logo_ff', banner_url: '@drawable/banner_ff',
    gradient_start: '#065F46', gradient_end: '#10B981', accent_color: '#34D399',
    is_popular: 1, is_new: 0, requires_zone_id: 0, sort_order: 2,
  },
  {
    name: 'PUBG Mobile', slug: 'pubg-mobile', category_slug: 'battle-royale',
    description: 'Game battle royale legendaris. Top up UC PUBG Mobile dengan cepat.',
    icon_url: '@drawable/logo_pubg', banner_url: '@drawable/banner_pubg',
    gradient_start: '#78350F', gradient_end: '#F59E0B', accent_color: '#FBBF24',
    is_popular: 0, is_new: 0, requires_zone_id: 0, sort_order: 3,
  },
  {
    name: 'Genshin Impact', slug: 'genshin-impact', category_slug: 'rpg',
    description: 'Game RPG open-world dari miHoYo. Top up Primogems Genshin Impact.',
    icon_url: '@drawable/logo_genshin', banner_url: '@drawable/banner_genshin',
    gradient_start: '#4C1D95', gradient_end: '#8B5CF6', accent_color: '#A78BFA',
    is_popular: 0, is_new: 1, requires_zone_id: 0, sort_order: 4,
  },
  {
    name: 'Valorant', slug: 'valorant', category_slug: 'fps',
    description: 'Game FPS taktis dari Riot Games. Top up VP Valorant harga terjangkau.',
    icon_url: '@drawable/logo_valoran', banner_url: '@drawable/banner_valloran',
    gradient_start: '#7F1D1D', gradient_end: '#EF4444', accent_color: '#F87171',
    is_popular: 0, is_new: 0, requires_zone_id: 0, sort_order: 5,
  },
  {
    name: 'Honor of Kings', slug: 'honor-of-kings', category_slug: 'moba',
    description: 'Game MOBA mobile dari Tencent. Top up Token HoK dengan mudah.',
    icon_url: '@drawable/logo_hok', banner_url: '@drawable/banner_hok',
    gradient_start: '#1E3A5F', gradient_end: '#0EA5E9', accent_color: '#38BDF8',
    is_popular: 0, is_new: 1, requires_zone_id: 0, sort_order: 6,
  },
  {
    name: 'Clash of Clans', slug: 'clash-of-clans', category_slug: 'strategy',
    description: 'Game strategi iconic dari Supercell. Top up Permata CoC murah.',
    icon_url: '@drawable/logo_coc', banner_url: '@drawable/banner_coc',
    gradient_start: '#3B1A08', gradient_end: '#D97706', accent_color: '#F59E0B',
    is_popular: 0, is_new: 0, requires_zone_id: 0, sort_order: 7,
  },
  {
    name: 'League of Legends', slug: 'league-of-legends', category_slug: 'moba',
    description: 'Game MOBA PC populer dari Riot Games. Top up RP LoL dengan harga terbaik.',
    icon_url: '@drawable/logo_lol', banner_url: '@drawable/banner_lol',
    gradient_start: '#0C1A4A', gradient_end: '#2563EB', accent_color: '#60A5FA',
    is_popular: 0, is_new: 0, requires_zone_id: 0, sort_order: 8,
  },
  {
    name: 'Call of Duty Mobile', slug: 'call-of-duty-mobile', category_slug: 'fps',
    description: 'Game FPS mobile dari Activision. Top up CP CODM dengan mudah.',
    icon_url: '@drawable/logo_cod', banner_url: '@drawable/banner_codm',
    gradient_start: '#111827', gradient_end: '#374151', accent_color: '#9CA3AF',
    is_popular: 0, is_new: 0, requires_zone_id: 0, sort_order: 9,
  },
  {
    name: 'FC 25', slug: 'fc-25', category_slug: 'sport',
    description: 'Game olahraga sepak bola terbaru. Top up FC Points dengan mudah.',
    icon_url: '@drawable/fc_logo', banner_url: '@drawable/banner_fc',
    gradient_start: '#064E3B', gradient_end: '#10B981', accent_color: '#6EE7B7',
    is_popular: 0, is_new: 0, requires_zone_id: 0, sort_order: 10,
  },
  {
    name: 'Magic Chess: Go Go', slug: 'magic-chess-go-go', category_slug: 'moba',
    description: 'Game auto-battler seru. Top up DM Points dengan mudah.',
    icon_url: '@drawable/mcgg_logo', banner_url: '@drawable/banner_mcgg',
    gradient_start: '#4C1D95', gradient_end: '#7C3AED', accent_color: '#DDD6FE',
    is_popular: 0, is_new: 0, requires_zone_id: 0, sort_order: 11,
  },
  {
    name: 'Roblox', slug: 'roblox', category_slug: 'platform',
    description: 'Platform game virtual. Top up Robux dengan mudah.',
    icon_url: '@drawable/logo_roblox', banner_url: '@drawable/banner_roblox',
    gradient_start: '#374151', gradient_end: '#4B5563', accent_color: '#9CA3AF',
    is_popular: 0, is_new: 0, requires_zone_id: 0, sort_order: 12,
  },
];

const seedGames = db.transaction(() => {
  for (const g of games) {
    const catRow = getCategoryId.get(g.category_slug);
    if (!catRow) throw new Error(`Category not found: ${g.category_slug}`);
    const { category_slug, ...gameData } = g;
    insertGame.run({ ...gameData, category_id: catRow.id });
  }
});
seedGames();
console.log('✅ Games seeded');

// ====================
// TOP-UP PACKAGES
// ====================
const insertPackage = db.prepare(`
  INSERT OR IGNORE INTO packages
    (game_id, label, amount, bonus, price, is_popular, sort_order)
  VALUES
    (@game_id, @label, @amount, @bonus, @price, @is_popular, @sort_order)
`);

const getGameId = db.prepare('SELECT id FROM games WHERE slug = ?');

const allPackages = {
  'mobile-legends': [
    { label: '86 Diamonds',   amount: 86,   bonus: 0,   price: 18000,  is_popular: 0,  sort_order: 1 },
    { label: '172 Diamonds',  amount: 172,  bonus: 10,  price: 35000,  is_popular: 0,  sort_order: 2 },
    { label: '257 Diamonds',  amount: 257,  bonus: 20,  price: 52000,  is_popular: 0,  sort_order: 3 },
    { label: '344 Diamonds',  amount: 344,  bonus: 30,  price: 69000,  is_popular: 1,  sort_order: 4 },
    { label: '429 Diamonds',  amount: 429,  bonus: 40,  price: 86000,  is_popular: 0,  sort_order: 5 },
    { label: '514 Diamonds',  amount: 514,  bonus: 50,  price: 103000, is_popular: 0,  sort_order: 6 },
    { label: '706 Diamonds',  amount: 706,  bonus: 70,  price: 140000, is_popular: 0,  sort_order: 7 },
    { label: '878 Diamonds',  amount: 878,  bonus: 100, price: 174000, is_popular: 0,  sort_order: 8 },
    { label: '1412 Diamonds', amount: 1412, bonus: 150, price: 280000, is_popular: 0,  sort_order: 9 },
    { label: '2195 Diamonds', amount: 2195, bonus: 250, price: 434000, is_popular: 0,  sort_order: 10 },
    { label: '3688 Diamonds', amount: 3688, bonus: 400, price: 730000, is_popular: 0,  sort_order: 11 },
    { label: '5532 Diamonds', amount: 5532, bonus: 700, price: 1095000,is_popular: 0,  sort_order: 12 },
  ],
  'free-fire': [
    { label: '50 Diamond',    amount: 50,   bonus: 0,  price: 8000,   is_popular: 0, sort_order: 1 },
    { label: '100 Diamond',   amount: 100,  bonus: 10, price: 15000,  is_popular: 0, sort_order: 2 },
    { label: '210 Diamond',   amount: 210,  bonus: 20, price: 30000,  is_popular: 1, sort_order: 3 },
    { label: '310 Diamond',   amount: 310,  bonus: 30, price: 44000,  is_popular: 0, sort_order: 4 },
    { label: '520 Diamond',   amount: 520,  bonus: 50, price: 73000,  is_popular: 0, sort_order: 5 },
    { label: '1060 Diamond',  amount: 1060, bonus: 100,price: 148000, is_popular: 0, sort_order: 6 },
  ],
  'pubg-mobile': [
    { label: '60 UC',   amount: 60,   bonus: 0, price: 15000,  is_popular: 0, sort_order: 1 },
    { label: '120 UC',  amount: 120,  bonus: 0, price: 30000,  is_popular: 0, sort_order: 2 },
    { label: '325 UC',  amount: 325,  bonus: 0, price: 79000,  is_popular: 1, sort_order: 3 },
    { label: '660 UC',  amount: 660,  bonus: 0, price: 158000, is_popular: 0, sort_order: 4 },
    { label: '1800 UC', amount: 1800, bonus: 0, price: 429000, is_popular: 0, sort_order: 5 },
    { label: '3850 UC', amount: 3850, bonus: 0, price: 859000, is_popular: 0, sort_order: 6 },
  ],
  'fc-25': [
    { label: '100 FC Points',  amount: 100,  bonus: 0,   price: 15000,  is_popular: 0, sort_order: 1 },
    { label: '500 FC Points',  amount: 500,  bonus: 25,  price: 75000,  is_popular: 0, sort_order: 2 },
    { label: '1050 FC Points', amount: 1050, bonus: 50,  price: 149000, is_popular: 1, sort_order: 3 },
    { label: '2800 FC Points', amount: 2800, bonus: 200, price: 399000, is_popular: 0, sort_order: 4 },
    { label: '5750 FC Points', amount: 5750, bonus: 500, price: 799000, is_popular: 0, sort_order: 5 },
  ],
  'magic-chess-go-go': [
    { label: '50 DM Points',   amount: 50,   bonus: 0,   price: 10000,  is_popular: 0, sort_order: 1 },
    { label: '100 DM Points',  amount: 100,  bonus: 5,   price: 19000,  is_popular: 0, sort_order: 2 },
    { label: '250 DM Points',  amount: 250,  bonus: 15,  price: 45000,  is_popular: 1, sort_order: 3 },
    { label: '500 DM Points',  amount: 500,  bonus: 40,  price: 89000,  is_popular: 0, sort_order: 4 },
    { label: '1000 DM Points', amount: 1000, bonus: 100, price: 175000, is_popular: 0, sort_order: 5 },
  ],
  'roblox': [
    { label: '400 Robux',      amount: 400,  bonus: 0,   price: 79000,  is_popular: 0, sort_order: 1 },
    { label: '800 Robux',      amount: 800,  bonus: 0,   price: 159000, is_popular: 1, sort_order: 2 },
    { label: '1700 Robux',     amount: 1700, bonus: 0,   price: 329000, is_popular: 0, sort_order: 3 },
    { label: '4500 Robux',     amount: 4500, bonus: 0,   price: 799000, is_popular: 0, sort_order: 4 },
    { label: '10000 Robux',    amount: 10000,bonus: 0,   price: 1599000,is_popular: 0, sort_order: 5 },
  ],
  // Default packages for games without specific packages
  '_default': [
    { label: 'Starter Pack', amount: 100,  bonus: 0,   price: 15000,  is_popular: 0, sort_order: 1 },
    { label: 'Basic Pack',   amount: 250,  bonus: 20,  price: 35000,  is_popular: 0, sort_order: 2 },
    { label: 'Value Pack',   amount: 500,  bonus: 50,  price: 65000,  is_popular: 1, sort_order: 3 },
    { label: 'Pro Pack',     amount: 1000, bonus: 100, price: 125000, is_popular: 0, sort_order: 4 },
    { label: 'Elite Pack',   amount: 2000, bonus: 200, price: 245000, is_popular: 0, sort_order: 5 },
    { label: 'Ultimate Pack',amount: 5000, bonus: 500, price: 599000, is_popular: 0, sort_order: 6 },
  ],
};

const seedPackages = db.transaction(() => {
  for (const g of games) {
    const gameRow = getGameId.get(g.slug);
    if (!gameRow) continue;
    const pkgList = allPackages[g.slug] || allPackages['_default'];
    for (const pkg of pkgList) {
      insertPackage.run({ ...pkg, game_id: gameRow.id });
    }
  }
});
seedPackages();
console.log('✅ Packages seeded');

// ====================
// BANNERS
// ====================
const insertBanner = db.prepare(`
  INSERT OR IGNORE INTO banners
    (game_id, title, subtitle, discount_text, image_url, gradient_start, gradient_end, accent_color, sort_order)
  VALUES
    (@game_id, @title, @subtitle, @discount_text, @image_url, @gradient_start, @gradient_end, @accent_color, @sort_order)
`);

const banners = [
    { game_slug: 'mobile-legends',    title: 'Mobile Legends', subtitle: 'Top Up Diamond & Dapatkan Bonus Ekstra!',      discount_text: 'BONUS 20%',     image_url: '@drawable/banner_ml', gradient_start: '#1E3A8A', gradient_end: '#7C3AED', accent_color: '#60A5FA', sort_order: 1 },
    { game_slug: 'free-fire',         title: 'Free Fire',      subtitle: 'Flash Sale Diamond FF Hanya Hari Ini!',        discount_text: 'HEMAT 15%',     image_url: '@drawable/banner_ff', gradient_start: '#064E3B', gradient_end: '#0E7490', accent_color: '#34D399', sort_order: 2 },
    { game_slug: 'pubg-mobile',       title: 'PUBG Mobile',    subtitle: 'Beli UC Sekarang & Raih Outfit Legendary!',    discount_text: 'PROMO SPESIAL', image_url: '@drawable/banner_pubg', gradient_start: '#78350F', gradient_end: '#B45309', accent_color: '#FBBF24', sort_order: 3 },
    { game_slug: 'genshin-impact',    title: 'Genshin Impact', subtitle: 'Flash Sale Primogems Genshin Hanya Hari Ini!', discount_text: 'HEMAT 15%',     image_url: '@drawable/banner_genshin', gradient_start: '#064E3B', gradient_end: '#0E7490', accent_color: '#34D399', sort_order: 4 },
    { game_slug: 'valorant',          title: 'Valorant',       subtitle: 'Top Up VP & Raih Skin Legendary!',             discount_text: 'PROMO SPESIAL', image_url: '@drawable/banner_valloran', gradient_start: '#78350F', gradient_end: '#B45309', accent_color: '#FBBF24', sort_order: 5 },
    { game_slug: 'honor-of-kings',    title: 'Honor of Kings', subtitle: 'Flash Sale Token HoK Hanya Hari Ini!',         discount_text: 'HEMAT 20%',     image_url: '@drawable/banner_hok', gradient_start: '#064E3B', gradient_end: '#0E7490', accent_color: '#34D399', sort_order: 6 },
    { game_slug: 'clash-of-clans',    title: 'Clash of Clans', subtitle: 'Flash Sale Permata CoC Hanya Hari Ini!',       discount_text: 'HEMAT 10%',     image_url: '@drawable/banner_coc', gradient_start: '#064E3B', gradient_end: '#0E7490', accent_color: '#34D399', sort_order: 7 },
    { game_slug: 'league-of-legends', title: 'League of Legends', subtitle: 'Flash Sale RP LoL Hanya Hari Ini!',         discount_text: 'HEMAT 30%',     image_url: '@drawable/banner_lol', gradient_start: '#064E3B', gradient_end: '#0E7490', accent_color: '#34D399', sort_order: 8 },
    { game_slug: 'call-of-duty-mobile', title: 'Call of Duty Mobile', subtitle: 'Flash Sale CP CODM Hanya Hari Ini!',   discount_text: 'HEMAT 15%',     image_url: '@drawable/banner_codm', gradient_start: '#064E3B', gradient_end: '#0E7490', accent_color: '#34D399', sort_order: 9 },
    { game_slug: 'fc-25',             title: 'FC 25',          subtitle: 'Flash Sale FC Points Hanya Hari Ini!',         discount_text: 'HEMAT 15%',     image_url: '@drawable/banner_fc', gradient_start: '#065F46', gradient_end: '#059669', accent_color: '#6EE7B7', sort_order: 10 },
    { game_slug: 'magic-chess-go-go', title: 'Magic Chess: Go Go', subtitle: 'Flash Sale DM Points Hanya Hari Ini!',         discount_text: 'HEMAT 15%',     image_url: '@drawable/banner_mcgg', gradient_start: '#5B21B6', gradient_end: '#8B5CF6', accent_color: '#DDD6FE', sort_order: 11 },
    { game_slug: 'roblox',             title: 'Roblox',         subtitle: 'Flash Sale Robux Hanya Hari Ini!',             discount_text: 'HEMAT 15%',     image_url: '@drawable/banner_roblox', gradient_start: '#374151', gradient_end: '#111827', accent_color: '#9CA3AF', sort_order: 12 },
  ];

  const checkBannerExists = db.prepare('SELECT id FROM banners WHERE title = ?');

  const seedBanners = db.transaction(() => {
    for (const b of banners) {
      const existing = checkBannerExists.get(b.title);
      if (!existing) {
        const gameRow = getGameId.get(b.game_slug);
        const { game_slug, ...bannerData } = b;
        insertBanner.run({ ...bannerData, game_id: gameRow ? gameRow.id : null });
      }
    }
  });
  seedBanners();
  console.log('✅ Banners seeded');

// ====================
// DEMO USERS
// ====================
const insertUser = db.prepare(`
  INSERT OR IGNORE INTO users (id, name, email, phone, password, role)
  VALUES (@id, @name, @email, @phone, @password, @role)
`);

const demoUsers = [
  {
    id: uuidv4(),
    name: 'Admin ARZ',
    email: 'admin@arzstore.id',
    phone: '+628100000000',
    password: bcrypt.hashSync('admin123', 10),
    role: 'admin',
  },
  {
    id: uuidv4(),
    name: 'ARZ User',
    email: 'user@arzstore.id',
    phone: '+6281234567890',
    password: bcrypt.hashSync('user123', 10),
    role: 'user',
  },
];

const seedUsers = db.transaction(() => {
  for (const u of demoUsers) insertUser.run(u);
});
seedUsers();
console.log('✅ Demo users seeded');

// ====================
// DEMO TRANSACTIONS
// ====================
const userRow = db.prepare('SELECT id FROM users WHERE email = ?').get('user@arzstore.id');
const mlGame = db.prepare('SELECT id FROM games WHERE slug = ?').get('mobile-legends');
const ffGame = db.prepare('SELECT id FROM games WHERE slug = ?').get('free-fire');
const pubgGame = db.prepare('SELECT id FROM games WHERE slug = ?').get('pubg-mobile');

const mlPkg = db.prepare('SELECT id FROM packages WHERE game_id = ? AND label = ?').get(mlGame?.id, '344 Diamonds');
const ffPkg = db.prepare('SELECT id FROM packages WHERE game_id = ? AND label = ?').get(ffGame?.id, '210 Diamond');
const pubgPkg = db.prepare('SELECT id FROM packages WHERE game_id = ? AND label = ?').get(pubgGame?.id, '325 UC');

const checkTx = db.prepare('SELECT COUNT(*) as cnt FROM transactions');
const txCount = checkTx.get().cnt;

if (txCount === 0 && userRow && mlPkg && ffPkg && pubgPkg) {
  const insertTx = db.prepare(`
    INSERT INTO transactions
      (id, user_id, game_id, package_id, game_user_id, game_zone_id,
       payment_method, amount, bonus, price, status, created_at)
    VALUES
      (@id, @user_id, @game_id, @package_id, @game_user_id, @game_zone_id,
       @payment_method, @amount, @bonus, @price, @status, @created_at)
  `);

  const demoTxns = [
    {
      id: uuidv4(),
      user_id: userRow.id, game_id: mlGame.id, package_id: mlPkg.id,
      game_user_id: '123456789', game_zone_id: '1234',
      payment_method: 'Dana', amount: 344, bonus: 30, price: 69000,
      status: 'success', created_at: '2026-04-13T10:30:00',
    },
    {
      id: uuidv4(),
      user_id: userRow.id, game_id: ffGame.id, package_id: ffPkg.id,
      game_user_id: '987654321', game_zone_id: null,
      payment_method: 'GoPay', amount: 210, bonus: 20, price: 30000,
      status: 'success', created_at: '2026-04-12T15:22:00',
    },
    {
      id: uuidv4(),
      user_id: userRow.id, game_id: pubgGame.id, package_id: pubgPkg.id,
      game_user_id: '555123456', game_zone_id: null,
      payment_method: 'OVO', amount: 325, bonus: 0, price: 79000,
      status: 'success', created_at: '2026-04-10T08:11:00',
    },
  ];

  const seedTx = db.transaction(() => {
    for (const tx of demoTxns) insertTx.run(tx);
  });
  seedTx();
  console.log('✅ Demo transactions seeded');
} else {
  console.log('⏭️  Transactions already exist, skipping');
}

console.log('\n🎉 Database seeding complete!');
console.log('\nDemo accounts:');
console.log('  Admin  → email: admin@arzstore.id  | password: admin123');
console.log('  User   → email: user@arzstore.id   | password: user123');
