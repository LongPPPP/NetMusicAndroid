import {Router} from 'express';
import * as authController from '../controllers/auth.controller';
import {authLimiter} from '../middlewares/rateLimiter.middleware';
import {validate} from '../middlewares/validate';
import {loginSchema, refreshTokenSchema, registerSchema} from '../validators/auth.validator';

const router = Router();

router.post('/register', authLimiter, validate(registerSchema), authController.register);
router.post('/login', authLimiter, validate(loginSchema), authController.login);
router.post('/refresh', validate(refreshTokenSchema), authController.refresh);

export default router;
