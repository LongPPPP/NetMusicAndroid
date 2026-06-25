import {Router} from 'express';
import * as playlistController from '../controllers/playlist.controller';
import {authMiddleware} from '../middlewares/auth.middleware';
import {validate} from '../middlewares/validate';
import {addPlaylistSongSchema, createPlaylistSchema, updatePlaylistSchema} from '../validators/playlist.validator';

const router = Router();

router.get('/:playlistId', playlistController.getPlaylistDetail);
router.post('/', authMiddleware, validate(createPlaylistSchema), playlistController.createPlaylist);
router.patch('/:playlistId', authMiddleware, validate(updatePlaylistSchema), playlistController.renamePlaylist);
router.post('/:playlistId/songs', authMiddleware, validate(addPlaylistSongSchema), playlistController.addSongToPlaylist);
router.delete('/:playlistId/songs/:songId', authMiddleware, playlistController.removeSongFromPlaylist);
router.delete('/:playlistId', authMiddleware, playlistController.deletePlaylist);

export default router;
