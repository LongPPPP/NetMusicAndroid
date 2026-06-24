import fs from 'fs';
import path from 'path';
import prisma from '../config/database';
import {asyncHandler} from '../utils/asyncHandler';
import {success} from '../utils/response';

/** 上传头像 */
export const uploadAvatar = asyncHandler(async (req, res) => {
    if (!req.file) {
        return success(res, null, '请选择要上传的文件', 400);
    }

    const url = `/static/avatars/${req.file.filename}`;

    // 查出旧头像，清理本地旧文件
    const oldUser = await prisma.user.findUnique({
        where: {id: req.userId},
        select: {avatar: true},
    });

    if (oldUser?.avatar?.startsWith('/static/avatars/')) {
        const oldPath = path.resolve(__dirname, '../../storage/avatars', path.basename(oldUser.avatar));
        fs.unlink(oldPath, () => {}); // 忽略删除失败（文件可能已被删）
    }

    // 更新数据库
    await prisma.user.update({
        where: {id: req.userId},
        data: {avatar: url},
    });

    return success(res, {url}, '上传成功');
});
