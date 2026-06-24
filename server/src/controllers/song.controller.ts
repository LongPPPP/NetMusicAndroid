import * as songService from '../services/song.service';
import {asyncHandler} from '../utils/asyncHandler';
import {success} from '../utils/response';

// 获取歌曲详情
export const getSongDetail = asyncHandler(async (req, res) => {
    const songId = parseInt(String(req.params.id), 10);
    const result = await songService.getSongDetail(songId);
    return success(res, result);
});

// 获取歌曲评论列表
export const getSongComments = asyncHandler(async (req, res) => {
    const songId = parseInt(String(req.params.id), 10);
    const result = await songService.getSongComments(songId);
    return success(res, result);
});

// 发表评论
export const createComment = asyncHandler(async (req, res) => {
    const songId = parseInt(String(req.params.id), 10);
    const result = await songService.createComment(songId, req.body);
    return success(res, result, '评论成功', 201);
});
