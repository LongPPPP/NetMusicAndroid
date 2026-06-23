import * as authService from '../services/auth.service';
import { asyncHandler } from '../utils/asyncHandler';
import { success } from '../utils/response';

// 用户注册
export const register = asyncHandler(async (req, res) => {
  const result = await authService.register(req.body);
  return success(res, result, '注册成功', 201);
});

// 用户登录
export const login = asyncHandler(async (req, res) => {
  const result = await authService.login(req.body);
  return success(res, result, '登录成功');
});

// 验证 Token 有效性（免登自动登录）
export const verifyToken = asyncHandler(async (req, res) => {
  const user = await authService.getUserBasicInfo(req.userId!);
  return success(res, user, 'Token 有效');
});
