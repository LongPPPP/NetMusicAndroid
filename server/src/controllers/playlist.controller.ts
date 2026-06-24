import * as playlistService from '../services/playlist.service';
import {asyncHandler} from '../utils/asyncHandler';
import {success} from '../utils/response';

// 获取用户收藏的歌单列表
export const getPlaylistsByUser = asyncHandler(async (req, res) => {
    const userId = parseInt(String(req.query.user_id), 10);
    const result = await playlistService.getPlaylistsByUser(userId);
    return success(res, result);
});

// 获取歌单详情（含歌曲列表）
export const getPlaylistDetail = asyncHandler(async (req, res) => {
    const playlistId = parseInt(String(req.params.id), 10);
    const result = await playlistService.getPlaylistDetail(playlistId);
    return success(res, result);
});

// 创建歌单
export const createPlaylist = asyncHandler(async (req, res) => {
    const result = await playlistService.createPlaylist(req.body);
    return success(res, result, '创建歌单成功', 201);
});
