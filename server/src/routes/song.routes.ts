import {Router} from 'express';
import * as songController from '../controllers/song.controller';
import {authMiddleware} from '../middlewares/auth.middleware';
import {validate, validateQuery} from '../middlewares/validate';
import {createCommentSchema, getCommentsSchema, getSongsSchema} from '../validators/song.validator';

const router = Router();

router.get('/', validateQuery(getSongsSchema), songController.listSongs);
router.get('/:songId', songController.getSongDetail);
router.get('/:songId/comments', validateQuery(getCommentsSchema), songController.getSongComments);
router.post('/:songId/comments', authMiddleware, validate(createCommentSchema), songController.createComment);
router.delete('/comments/:commentId', authMiddleware, songController.deleteComment);

export default router;