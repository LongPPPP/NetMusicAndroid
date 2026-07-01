import {Router} from 'express';
import * as playlistController from '../controllers/playlist.controller';
import * as userController from '../controllers/user.controller';
import {authMiddleware} from '../middlewares/auth.middleware';
import {requireRole} from '../middlewares/role.middleware';
import {uploadAvatar as uploadAvatarMiddleware} from '../middlewares/upload.middleware';
import {validate} from '../middlewares/validate';
import {updateUserSchema} from '../validators/user.validator';

const router = Router();

// 需鉴权 — 放在 /:userId 前面，避免被通配匹配
router.get('/me', authMiddleware, userController.getMyProfile);
router.get('/me/playlists', authMiddleware, playlistController.getMyPlaylists);
router.get('/me/comments', authMiddleware, userController.getMyComments);
router.get('/me/favorites', authMiddleware, userController.getMyFavorites);
router.get('/me/singer', authMiddleware, userController.getMySingerId);
router.get('/me/songs', authMiddleware, requireRole('ARTIST'), userController.getMySongs);
router.put('/me/avatar', authMiddleware, uploadAvatarMiddleware, userController.updateAvatar);

// 公开
router.get('/:userId', userController.getProfile);
router.get('/:userId/playlists', playlistController.getPlaylistsByUser);
router.get('/:userId/favorites', userController.getFavorites);

// 数据字典统一修改接口
router.patch('/me', authMiddleware, validate(updateUserSchema), userController.updateUser);

export default router;
