import {ForbiddenError, ValidationError} from '../errors/AppError';
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

// 修改用户信息（仅本人）
export const updateProfile = asyncHandler(async (req, res) => {
    const userId = parseInt(String(req.params.id), 10);
    if (isNaN(userId)) {
        throw new ValidationError('用户 ID 格式不正确');
    }

    // 只能修改自己的资料
    if (userId !== req.userId) {
        throw new ForbiddenError('无权修改他人资料');
    }

    const user = await userService.updateUser(userId, req.body);
    return success(res, user, '更新成功');
});
