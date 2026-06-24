import {Router} from 'express';
import * as playlistController from '../controllers/playlist.controller';
import {authMiddleware} from '../middlewares/auth.middleware';
import {validate} from '../middlewares/validate';
import {createPlaylistSchema} from '../validators/playlist.validator';

const router = Router();

router.get('/', playlistController.getPlaylistsByUser);
router.get('/:id', playlistController.getPlaylistDetail);
router.post('/', authMiddleware, validate(createPlaylistSchema), playlistController.createPlaylist);

export default router;
