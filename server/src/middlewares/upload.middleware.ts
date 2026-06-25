import multer from 'multer';
import path from 'path';
import crypto from 'crypto';

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
const MAX_SIZE = 5 * 1024 * 1024; // 5MB

const storage = multer.diskStorage({
    destination: (_req, _file, cb) => {
        cb(null, path.resolve(__dirname, '../../static/avatars'));
    },
    filename: (_req, file, cb) => {
        const ext = path.extname(file.originalname) || '.jpg';
        cb(null, `${crypto.randomUUID()}${ext}`);
    },
});

const fileFilter = (_req: Express.Request, file: Express.Multer.File, cb: multer.FileFilterCallback) => {
    if (ALLOWED_TYPES.includes(file.mimetype)) {
        cb(null, true);
    } else {
        cb(new Error('仅支持 JPG/PNG/GIF/WebP 格式'));
    }
};

/** 头像上传中间件 — 单文件，字段名为 file */
export const uploadAvatar = multer({storage, fileFilter, limits: {fileSize: MAX_SIZE}}).single('file');
