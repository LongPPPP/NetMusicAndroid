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

// ---- 歌曲上传（封面 + 音频） ----

const COVER_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
const AUDIO_TYPES = ['audio/mpeg', 'audio/mp3', 'audio/wav', 'audio/ogg', 'audio/flac', 'audio/mp4', 'audio/aac'];
const SONG_MAX_SIZE = 20 * 1024 * 1024; // 20MB

const songStorage = multer.diskStorage({
    destination: (_req, file, cb) => {
        const dir = file.fieldname === 'cover'
            ? path.resolve(__dirname, '../../static/covers')
            : path.resolve(__dirname, '../../static/songs');
        cb(null, dir);
    },
    filename: (_req, file, cb) => {
        const ext = path.extname(file.originalname) || (file.fieldname === 'cover' ? '.jpg' : '.mp3');
        cb(null, `${crypto.randomUUID()}${ext}`);
    },
});

const songFileFilter = (_req: Express.Request, file: Express.Multer.File, cb: multer.FileFilterCallback) => {
    if (file.fieldname === 'cover' && COVER_TYPES.includes(file.mimetype)) {
        cb(null, true);
    } else if (file.fieldname === 'song' && AUDIO_TYPES.includes(file.mimetype)) {
        cb(null, true);
    } else {
        cb(new Error('仅支持 JPG/PNG/GIF/WebP 封面，MP3/WAV/OGG/FLAC/AAC 音频'));
    }
};

/** 歌曲上传中间件 — 双文件: cover（封面）+ song（音频） */
export const uploadSongFiles = multer({
    storage: songStorage,
    fileFilter: songFileFilter,
    limits: {fileSize: SONG_MAX_SIZE},
}).fields([
    {name: 'cover', maxCount: 1},
    {name: 'song', maxCount: 1},
]);
