import * as playlistService from '../services/playlist.service';
import {asyncHandler} from '../utils/asyncHandler';
import {success} from '../utils/response';

// 获取当前登录用户的歌单列表（通过 token）
export const getMyPlaylists = asyncHandler(async (req, res) => {
    const result = await playlistService.getPlaylistsByUser(req.userId!);
    return success(res, result);
});

// 获取用户收藏的歌单列表
export const getPlaylistsByUser = asyncHandler(async (req, res) => {
    const userId = parseInt(String(req.query.user_id), 10);
    const result = await playlistService.getPlaylistsByUser(userId);
    return success(res, result);
});

// 获取歌单详情（含歌曲列表）
export const getPlaylistDetail = asyncHandler(async (req, res) => {
    const playlistId = parseInt(String(req.params.playlistId), 10);
    const result = await playlistService.getPlaylistDetail(playlistId);
    return success(res, result);
});

// 创建歌单
export const createPlaylist = asyncHandler(async (req, res) => {
    const result = await playlistService.createPlaylist(req.userId!, req.body);
    return success(res, result, '创建歌单成功', 201);
});

// 歌单添加歌曲
export const addSongToPlaylist = asyncHandler(async (req, res) => {
    const playlistId = parseInt(String(req.params.playlistId), 10);
    const {song_id} = req.body;
    await playlistService.addSongToPlaylist(playlistId, song_id, req.userId!);
    return success(res, null, '添加成功', 201);
});

// 歌单移除歌曲
export const removeSongFromPlaylist = asyncHandler(async (req, res) => {
    const playlistId = parseInt(String(req.params.playlistId), 10);
    const songId = parseInt(String(req.params.songId), 10);
    await playlistService.removeSongFromPlaylist(playlistId, songId, req.userId!);
    return success(res, null, '移除成功');
});

// 重命名歌单
export const renamePlaylist = asyncHandler(async (req, res) => {
    const playlistId = parseInt(String(req.params.playlistId), 10);
    const result = await playlistService.renamePlaylist(playlistId, req.userId!, req.body);
    return success(res, result, '修改成功');
});

// 删除歌单
export const deletePlaylist = asyncHandler(async (req, res) => {
    const playlistId = parseInt(String(req.params.playlistId), 10);
    await playlistService.deletePlaylist(playlistId, req.userId!);
    return success(res, null, '删除成功');
});
