import { Request, Response } from 'express';
import * as authService from '../services/auth.service';
import { registerSchema, loginSchema } from '../validators/auth.validator';
import { AuthError } from '../constants/auth.errors';
import { success, fail } from '../utils/response';

// 用户注册
export async function register(req: Request, res: Response) {
  try {
    // Zod 校验参数（包含格式、长度、邮箱格式等）
    const parsed = registerSchema.safeParse(req.body);
    if (!parsed.success) {
      const firstError = parsed.error.issues[0];
      return fail(res, firstError.message);
    }

    const result = await authService.register(parsed.data);
    return success(res, result, '注册成功', 201);
  } catch (err: any) {
    if (err.message === AuthError.USERNAME_EXISTS) {
      return fail(res, '用户名已被占用');
    }
    if (err.message === AuthError.EMAIL_EXISTS) {
      return fail(res, '邮箱已被注册');
    }
    console.error('[Register Error]', err);
    return fail(res, '注册失败', 500);
  }
}

// 用户登录
export async function login(req: Request, res: Response) {
  try {
    // Zod 校验参数
    const parsed = loginSchema.safeParse(req.body);
    if (!parsed.success) {
      const firstError = parsed.error.issues[0];
      return fail(res, firstError.message);
    }

    const result = await authService.login(parsed.data);
    return success(res, result, '登录成功');
  } catch (err: any) {
    if (err.message === AuthError.USER_NOT_FOUND || err.message === AuthError.WRONG_PASSWORD) {
      return fail(res, '用户名或密码错误');
    }
    console.error('[Login Error]', err);
    return fail(res, '登录失败', 500);
  }
}
