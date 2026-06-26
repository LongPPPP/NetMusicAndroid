import * as songService from '../services/song.service';
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

// 上传/替换头像
export const updateAvatar = asyncHandler(async (req, res) => {
    if (!req.file) {
        return success(res, null, '请选择要上传的文件', 400);
    }

    const avatarUrl = `/static/avatars/${req.file.filename}`;
    await userService.updateAvatar(req.userId!, avatarUrl);

    return success(res, {url: avatarUrl}, '上传成功');
});

// 获取当前用户的所有评论
export const getMyComments = asyncHandler(async (req, res) => {
    const page = parseInt(String(req.query.page)) || 1;
    const pageSize = parseInt(String(req.query.page_size)) || 20;
    const result = await songService.getUserComments(req.userId!, page, pageSize);
    return success(res, result);
});

// 获取收藏列表（自己）
export const getMyFavorites = asyncHandler(async (req, res) => {
    const page = parseInt(String(req.query.page)) || 1;
    const pageSize = parseInt(String(req.query.page_size)) || 20;
    const result = await songService.getUserFavorites(req.userId!, page, pageSize);
    return success(res, result);
});

// 获取收藏列表（公开）
export const getFavorites = asyncHandler(async (req, res) => {
    const userId = parseInt(String(req.params.userId), 10);
    if (isNaN(userId)) {
        return success(res, null, '用户 ID 格式不正确', 400);
    }
    const page = parseInt(String(req.query.page)) || 1;
    const pageSize = parseInt(String(req.query.page_size)) || 20;
    const result = await songService.getUserFavorites(userId, page, pageSize);
    return success(res, result);
});
