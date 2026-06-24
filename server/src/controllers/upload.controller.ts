import {ValidationError} from '../errors/AppError';
import {asyncHandler} from '../utils/asyncHandler';
import {success} from '../utils/response';

/** 上传头像 */
export const uploadAvatar = asyncHandler(async (req, res) => {
    if (!req.file) {
        throw new ValidationError('请选择要上传的文件');
    }

    const url = `/uploads/avatars/${req.file.filename}`;
    return success(res, {url}, '上传成功');
});
