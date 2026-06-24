import {Router} from 'express';
import * as songController from '../controllers/song.controller';
import {authMiddleware} from '../middlewares/auth.middleware';
import {validate} from '../middlewares/validate';
import {createCommentSchema} from '../validators/song.validator';

const router = Router();

router.get('/:id', songController.getSongDetail);
router.get('/:id/comments', songController.getSongComments);
router.post('/:id/comments', authMiddleware, validate(createCommentSchema), songController.createComment);

export default router;
