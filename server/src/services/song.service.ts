import prisma from '../config/database';
import {CommentErrorMessage, SongErrorMessage} from '../constants/errorString';
import {ForbiddenError, NotFoundError} from '../errors/AppError';
import {sanitize} from '../utils/sanitize';
import type {CreateCommentInput, CreateSongInput, GetCommentsInput, GetSongsInput} from '../validators/song.validator';

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

// 分页获取歌曲列表
export async function listSongs(params: GetSongsInput) {
    const {page, page_size, singer_id} = params;
    const skip = (page - 1) * page_size;

    const where = singer_id ? {singerId: singer_id} : {};

    const [songs, total] = await Promise.all([
        prisma.song.findMany({
            where,
            select: {
                id: true,
                name: true,
                singerName: true,
                playUrl: true,
                duration: true,
            },
            skip,
            take: page_size,
            orderBy: {id: 'asc'},
        }),
        prisma.song.count({where}),
    ]);

    return {
        list: songs.map(s => ({
            song_id: s.id,
            song_name: s.name,
            singer_name: s.singerName,
            play_url: s.playUrl,
            duration: s.duration,
        })),
        total,
        page,
        page_size,
    };
}

// 分页获取歌曲评论列表（按时间倒序）
export async function getSongComments(songId: number, params: GetCommentsInput) {
    const song = await prisma.song.findUnique({
        where: {id: songId},
        select: {id: true},
    });
    if (!song) {
        throw new NotFoundError(CommentErrorMessage.SONG_NOT_FOUND);
    }

    const {page, page_size} = params;
    const skip = (page - 1) * page_size;

    const [comments, total] = await Promise.all([
        prisma.comment.findMany({
            where: {songId},
            select: {
                id: true,
                userId: true,
                username: true,
                content: true,
            },
            skip,
            take: page_size,
            orderBy: {createdAt: 'desc'},
        }),
        prisma.comment.count({where: {songId}}),
    ]);

    return {
        list: comments.map(c => ({
            comment_id: c.id,
            user_id: c.userId,
            username: c.username,
            content: c.content,
        })),
        total,
        page,
        page_size,
    };
}

// 发表评论
export async function createComment(songId: number, userId: number, data: CreateCommentInput) {
    const song = await prisma.song.findUnique({
        where: {id: songId},
        select: {id: true},
    });
    if (!song) {
        throw new NotFoundError(CommentErrorMessage.SONG_NOT_FOUND);
    }

    const content = sanitize(data.content);

    const user = await prisma.user.findUnique({
        where: {id: userId},
        select: {username: true},
    });

    const comment = await prisma.comment.create({
        data: {
            songId,
            userId,
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

// 删除评论
export async function deleteComment(songId: number, commentId: number, userId: number) {
    const comment = await prisma.comment.findUnique({
        where: {id: commentId},
        select: {songId: true, userId: true},
    });
    if (!comment || comment.songId !== songId) {
        throw new NotFoundError(CommentErrorMessage.NOT_FOUND);
    }
    if (comment.userId !== userId) {
        throw new ForbiddenError(CommentErrorMessage.NOT_AUTHOR);
    }

    await prisma.comment.delete({where: {id: commentId}});
}

// 获取当前用户的所有评论（按时间倒序）
export async function getUserComments(userId: number, page: number, pageSize: number) {
    const skip = (page - 1) * pageSize;

    const [comments, total] = await Promise.all([
        prisma.comment.findMany({
            where: {userId},
            select: {
                id: true,
                content: true,
                createdAt: true,
                song: {select: {id: true, name: true}},
            },
            skip,
            take: pageSize,
            orderBy: {createdAt: 'desc'},
        }),
        prisma.comment.count({where: {userId}}),
    ]);

    return {
        list: comments.map(c => ({
            comment_id: c.id,
            content: c.content,
            created_at: c.createdAt,
            song: {song_id: c.song.id, song_name: c.song.name},
        })),
        total,
        page,
        page_size: pageSize,
    };
}

// ===== ARTIST 歌曲管理 =====

// 校验用户是否为 ARTIST 并获取其 Singer 记录
async function assertArtist(userId: number) {
    const user = await prisma.user.findUnique({
        where: {id: userId},
        select: {role: true, singer: {select: {id: true, name: true}}},
    });
    if (!user || user.role !== 'ARTIST') {
        throw new ForbiddenError(SongErrorMessage.ARTIST_ONLY);
    }

    const singer = user.singer;
    if (!singer) {
        throw new NotFoundError(SongErrorMessage.NO_SINGER_PROFILE);
    }
    return singer;
}

// 上架歌曲
export async function createSong(userId: number, data: CreateSongInput) {
    const singer = await assertArtist(userId);

    const name = sanitize(data.name);
    const song = await prisma.song.create({
        data: {
            name,
            singerId: singer.id,
            singerName: data.singer_name || singer.name,
            playUrl: data.play_url,
            coverUrl: data.cover_url,
            duration: data.duration,
        },
        select: {
            id: true,
            name: true,
            singerName: true,
            playUrl: true,
            coverUrl: true,
            duration: true,
        },
    });

    return {
        song_id: song.id,
        song_name: song.name,
        singer_name: song.singerName,
        play_url: song.playUrl,
        cover_url: song.coverUrl,
        duration: song.duration,
    };
}

// 删除歌曲（仅歌手本人可删）
export async function deleteSong(songId: number, userId: number) {
    const singer = await assertArtist(userId);

    const song = await prisma.song.findUnique({
        where: {id: songId},
        select: {singerId: true},
    });
    if (!song) {
        throw new NotFoundError(SongErrorMessage.NOT_FOUND);
    }
    if (song.singerId !== singer.id) {
        throw new ForbiddenError(SongErrorMessage.CANNOT_DELETE_OTHERS_SONG);
    }

    await prisma.song.delete({where: {id: songId}});
}
