import {Router} from 'express';
import * as uploadController from '../controllers/upload.controller';
import {authMiddleware} from '../middlewares/auth.middleware';
import {uploadAvatar as uploadAvatarMiddleware} from '../middlewares/upload.middleware';

const router = Router();

router.post('/avatar', authMiddleware, uploadAvatarMiddleware, uploadController.uploadAvatar);

export default router;
