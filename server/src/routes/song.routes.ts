import {Router} from 'express';
import * as songController from '../controllers/song.controller';
import {authMiddleware} from '../middlewares/auth.middleware';
import {requireRole} from '../middlewares/role.middleware';
import {uploadSongFiles} from '../middlewares/upload.middleware';
import {validate, validateQuery} from '../middlewares/validate';
import {createCommentSchema, createSongSchema, getCommentsSchema, getSongsSchema} from '../validators/song.validator';

const router = Router();

// 公开
router.get('/', validateQuery(getSongsSchema), songController.listSongs);
router.get('/:songId', songController.getSongDetail);
router.get('/:songId/comments', validateQuery(getCommentsSchema), songController.getSongComments);

// 需鉴权
router.post('/:songId/comments', authMiddleware, validate(createCommentSchema), songController.createComment);
router.delete('/:songId/comments/:commentId', authMiddleware, songController.deleteComment);

// 收藏（toggle）
router.post('/:songId/favorite', authMiddleware, songController.toggleFavorite);

// 艺术家专属：上架/下架歌曲
router.post('/', authMiddleware, requireRole('ARTIST'), uploadSongFiles, validate(createSongSchema), songController.createSong);
router.delete('/:songId', authMiddleware, requireRole('ARTIST'), songController.deleteSong);

export default router;