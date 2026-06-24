import {ValidationError} from '../errors/AppError';
import * as userService from '../services/user.service';
import {asyncHandler} from '../utils/asyncHandler';
import {success} from '../utils/response';

// 获取用户信息（公开）
export const getProfile = asyncHandler(async (req, res) => {
    const userId = parseInt(String(req.params.id), 10);
    if (isNaN(userId)) {
        throw new ValidationError('用户 ID 格式不正确');
    }

    const user = await userService.getUserById(userId);
    return success(res, user);
});

// 获取当前登录用户信息
export const getMyProfile = asyncHandler(async (req, res) => {
    const user = await userService.getUserById(req.userId!);
    return success(res, user);
});

// 修改用户名
export const updateUsername = asyncHandler(async (req, res) => {
    const user = await userService.updateUsername(req.userId!, req.body.username);
    return success(res, user, '用户名修改成功');
});

// 修改头像
export const updateAvatar = asyncHandler(async (req, res) => {
    const user = await userService.updateAvatar(req.userId!, req.body.avatar);
    return success(res, user, '头像修改成功');
});

// 修改个性签名
export const updateSignature = asyncHandler(async (req, res) => {
    const user = await userService.updateSignature(req.userId!, req.body.signature);
    return success(res, user, '个性签名修改成功');
});

// 修改邮箱
export const updateEmail = asyncHandler(async (req, res) => {
    const user = await userService.updateEmail(req.userId!, req.body.email);
    return success(res, user, '邮箱修改成功');
});
