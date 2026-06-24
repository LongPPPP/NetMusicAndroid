import * as songService from '../services/song.service';
import {asyncHandler} from '../utils/asyncHandler';
import {success} from '../utils/response';

// 获取歌曲详情
export const getSongDetail = asyncHandler(async (req, res) => {
    const songId = parseInt(String(req.params.songId), 10);
    const result = await songService.getSongDetail(songId);
    return success(res, result);
});

// 分页获取歌曲列表
export const listSongs = asyncHandler(async (req, res) => {
    const result = await songService.listSongs(req.query as any);
    return success(res, result);
});

// 分页获取歌曲评论列表
export const getSongComments = asyncHandler(async (req, res) => {
    const songId = parseInt(String(req.params.songId), 10);
    const result = await songService.getSongComments(songId, req.query as any);
    return success(res, result);
});

// 发表评论
export const createComment = asyncHandler(async (req, res) => {
    const songId = parseInt(String(req.params.songId), 10);
    const result = await songService.createComment(songId, req.userId!, req.body);
    return success(res, result, '评论成功', 201);
});

// 删除评论
export const deleteComment = asyncHandler(async (req, res) => {
    const commentId = parseInt(String(req.params.commentId), 10);
    await songService.deleteComment(commentId, req.userId!);
    return success(res, null, '删除成功');
});
