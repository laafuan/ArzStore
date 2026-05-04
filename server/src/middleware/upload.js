// src/middleware/upload.js
// Multer file upload middleware for images (icons, banners)

const multer = require('multer');
const path = require('path');
const fs = require('fs');

// Ensure upload directories exist
const uploadBase = path.join(__dirname, '../../uploads');
const dirs = ['icons', 'banners', 'avatars'];
dirs.forEach(dir => {
  const p = path.join(uploadBase, dir);
  if (!fs.existsSync(p)) fs.mkdirSync(p, { recursive: true });
});

/**
 * Configure multer disk storage
 */
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    let folder = 'icons';
    if (req.baseUrl.includes('banners')) folder = 'banners';
    if (req.baseUrl.includes('users') || req.path.includes('avatar')) folder = 'avatars';
    cb(null, path.join(uploadBase, folder));
  },
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase();
    const name = `${Date.now()}-${Math.round(Math.random() * 1e6)}${ext}`;
    cb(null, name);
  },
});

/**
 * File filter: images only
 */
const imageFilter = (req, file, cb) => {
  const allowed = /jpeg|jpg|png|webp|gif/;
  const isAllowed = allowed.test(path.extname(file.originalname).toLowerCase())
    && allowed.test(file.mimetype);
  if (isAllowed) cb(null, true);
  else cb(new Error('Hanya file gambar yang diizinkan (jpg, png, webp, gif)'));
};

const upload = multer({
  storage,
  fileFilter: imageFilter,
  limits: { fileSize: 5 * 1024 * 1024 }, // 5 MB
});

module.exports = upload;
