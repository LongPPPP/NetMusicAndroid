import * as authService from '../services/auth.service';
import {asyncHandler} from '../utils/asyncHandler';
import {success} from '../utils/response';

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

// 刷新 Access Token
export const refresh = asyncHandler(async (req, res) => {
    const {refreshToken} = req.body;
    const result = await authService.refresh(refreshToken);
    return success(res, result, 'Token 刷新成功');
});
