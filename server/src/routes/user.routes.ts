import {Router} from 'express';
import * as userController from '../controllers/user.controller';
import {authMiddleware} from '../middlewares/auth.middleware';
import {validate} from '../middlewares/validate';
import {updateUserSchema} from '../validators/user.validator';

const router = Router();

router.get('/:id', userController.getProfile);                                     // 公开：查看用户信息
router.put('/:id', authMiddleware, validate(updateUserSchema), userController.updateProfile);  // 需鉴权：修改自己的信息

export default router;
