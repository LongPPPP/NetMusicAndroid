import {Router} from 'express';
import * as authController from '../controllers/auth.controller';
import {authMiddleware} from '../middlewares/auth.middleware';
import {authLimiter} from '../middlewares/rateLimiter.middleware';
import {validate} from '../middlewares/validate';
import {loginSchema, registerSchema} from '../validators/auth.validator';

const router = Router();

router.post('/register', authLimiter, validate(registerSchema), authController.register);
router.post('/login', authLimiter, validate(loginSchema), authController.login);
router.post('/verify-token', authMiddleware, authController.verifyToken);

export default router;
