import prisma from '../config/database';
import {CommentErrorMessage, SongErrorMessage} from '../constants/errorString';
import {NotFoundError} from '../errors/AppError';
import {sanitize} from '../utils/sanitize';
import type {CreateCommentInput} from '../validators/song.validator';

// 获取歌曲详情
export async function getSongDetail(songId: number) {
    const song = await prisma.song.findUnique({
        where: {id: songId},
        select: {
            id: true,
            name: true,
            singerName: true,
            playUrl: true,
            coverUrl: true,
            duration: true,
        },
    });

    if (!song) {
        throw new NotFoundError(SongErrorMessage.NOT_FOUND);
    }

    return {
        song_id: song.id,
        song_name: song.name,
        singer_name: song.singerName,
        play_url: song.playUrl,
        cover_url: song.coverUrl,
        duration: song.duration,
    };
}

// 获取歌曲评论列表（按时间倒序）
export async function getSongComments(songId: number) {
    const song = await prisma.song.findUnique({
        where: {id: songId},
        select: {id: true},
    });
    if (!song) {
        throw new NotFoundError(CommentErrorMessage.SONG_NOT_FOUND);
    }

    const comments = await prisma.comment.findMany({
        where: {songId},
        select: {
            id: true,
            userId: true,
            username: true,
            content: true,
            createdAt: true,
        },
        orderBy: {createdAt: 'desc'},
    });

    return {
        list: comments.map(c => ({
            comment_id: c.id,
            user_id: c.userId,
            username: c.username,
            content: c.content,
        })),
    };
}

// 发表评论
export async function createComment(songId: number, data: CreateCommentInput) {
    const song = await prisma.song.findUnique({
        where: {id: songId},
        select: {id: true},
    });
    if (!song) {
        throw new NotFoundError(CommentErrorMessage.SONG_NOT_FOUND);
    }

    const content = sanitize(data.content);

    // 获取用户名用于冗余存储
    const user = await prisma.user.findUnique({
        where: {id: data.user_id},
        select: {username: true},
    });

    const comment = await prisma.comment.create({
        data: {
            songId,
            userId: data.user_id,
            username: user?.username ?? null,
            content,
        },
        select: {
            id: true,
            userId: true,
            username: true,
            content: true,
            createdAt: true,
        },
    });

    return {
        comment_id: comment.id,
        user_id: comment.userId,
        username: comment.username,
        content: comment.content,
    };
}
