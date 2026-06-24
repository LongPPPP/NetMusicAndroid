import {Router} from 'express';
import * as userController from '../controllers/user.controller';
import {authMiddleware} from '../middlewares/auth.middleware';
import {validate} from '../middlewares/validate';
import {updateUserSchema} from '../validators/user.validator';

const router = Router();

// 需鉴权 — 放在 /:id 前面，避免被通配匹配
router.get('/me', authMiddleware, userController.getMyProfile);

// 公开
router.get('/:userId', userController.getProfile);

// 数据字典统一修改接口
router.patch('/me', authMiddleware, validate(updateUserSchema), userController.updateUser);

export default router;
