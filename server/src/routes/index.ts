import {Router} from 'express';
import authRoutes from './auth.routes';
import uploadRoutes from './upload.routes';
import userRoutes from './user.routes';
import playlistRoutes from './playlist.routes';
import songRoutes from './song.routes';
import singerRoutes from './singer.routes';
import searchRoutes from './search.routes';

const router = Router();

router.use('/auth', authRoutes);
router.use('/users', userRoutes);
router.use('/upload', uploadRoutes);
router.use('/playlists', playlistRoutes);
router.use('/songs', songRoutes);
router.use('/singers', singerRoutes);
router.use('/search', searchRoutes);

export default router;
