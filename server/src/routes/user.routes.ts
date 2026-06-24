import {Router} from 'express';
import * as userController from '../controllers/user.controller';
import {authMiddleware} from '../middlewares/auth.middleware';
import {validate} from '../middlewares/validate';
import {
    updateAvatarSchema,
    updateEmailSchema,
    updateSignatureSchema,
    updateUsernameSchema,
} from '../validators/user.validator';

const router = Router();

// 需鉴权 — 放在 /:id 前面，避免被通配匹配
router.get('/me', authMiddleware, userController.getMyProfile);
router.patch('/me/username', authMiddleware, validate(updateUsernameSchema), userController.updateUsername);
router.patch('/me/avatar', authMiddleware, validate(updateAvatarSchema), userController.updateAvatar);
router.patch('/me/signature', authMiddleware, validate(updateSignatureSchema), userController.updateSignature);
router.patch('/me/email', authMiddleware, validate(updateEmailSchema), userController.updateEmail);

// 公开
router.get('/:id', userController.getProfile);

export default router;
