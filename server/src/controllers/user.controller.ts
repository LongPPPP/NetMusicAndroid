import * as userService from '../services/user.service';
import {asyncHandler} from '../utils/asyncHandler';
import {success} from '../utils/response';

// 获取用户信息（公开）
export const getProfile = asyncHandler(async (req, res) => {
    const userId = parseInt(String(req.params.userId), 10);
    if (isNaN(userId)) {
        return success(res, null, '用户 ID 格式不正确', 400);
    }

    const user = await userService.getUserById(userId);
    return success(res, user);
});

// 获取当前登录用户信息
export const getMyProfile = asyncHandler(async (req, res) => {
    const user = await userService.getUserById(req.userId!);
    return success(res, user);
});

// 统一修改用户信息
export const updateUser = asyncHandler(async (req, res) => {
    const {field, value} = req.body;
    const user = await userService.updateUser(req.userId!, field, value);
    return success(res, user, '修改成功');
});
