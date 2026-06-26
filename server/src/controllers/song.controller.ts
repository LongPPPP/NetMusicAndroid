import * as songService from '../services/song.service';
import prisma from '../config/database';
import {SongErrorMessage} from '../constants/errorString';
import {ValidationError} from '../errors/AppError';
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

// 上架歌曲
export const createSong = asyncHandler(async (req, res) => {
    const {name} = req.body;

    // 从上传文件中获取路径
    const files = req.files as {[fieldname: string]: Express.Multer.File[]} | undefined;
    const coverFile = files?.cover?.[0];
    const songFile = files?.song?.[0];

    if (!songFile) {
        throw new ValidationError('请上传歌曲文件');
    }

    const playUrl = `/static/songs/${songFile.filename}`;
    const coverUrl = coverFile ? `/static/covers/${coverFile.filename}` : null;

    // 通过 userId 找关联的 Singer
    const singer = await prisma.singer.findUnique({
        where: {userId: req.userId!},
        select: {id: true, name: true},
    });
    if (!singer) {
        throw new ValidationError(SongErrorMessage.NO_SINGER_PROFILE);
    }

    const result = await songService.createSong(name, singer.id, singer.name, playUrl, coverUrl);
    return success(res, result, '上架成功', 201);
});

// 下架歌曲
export const deleteSong = asyncHandler(async (req, res) => {
    const songId = parseInt(String(req.params.songId), 10);

    const singer = await prisma.singer.findUnique({
        where: {userId: req.userId!},
        select: {id: true},
    });
    if (!singer) {
        throw new ValidationError(SongErrorMessage.NO_SINGER_PROFILE);
    }

    await songService.deleteSong(songId, singer.id);
    return success(res, null, '下架成功');
});

// 收藏/取消收藏
export const toggleFavorite = asyncHandler(async (req, res) => {
    const songId = parseInt(String(req.params.songId), 10);
    const result = await songService.toggleFavorite(req.userId!, songId);
    return success(res, result, result.favorited ? '收藏成功' : '已取消收藏');
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
    const songId = parseInt(String(req.params.songId), 10);
    const commentId = parseInt(String(req.params.commentId), 10);
    await songService.deleteComment(songId, commentId, req.userId!);
    return success(res, null, '删除成功');
});
