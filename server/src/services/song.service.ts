import fs from 'fs';
import path from 'path';
import prisma from '../config/database';
import {CommentErrorMessage, SongErrorMessage} from '../constants/errorString';
import {ConflictError, ForbiddenError, NotFoundError, ValidationError} from '../errors/AppError';
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

// 上架歌曲
export async function createSong(name: string, singerId: number, singerName: string, playUrl: string, coverUrl: string | null) {
    const song = await prisma.song.create({
        data: {name, singerId, singerName, playUrl, coverUrl},
        select: {id: true, name: true, singerName: true, playUrl: true, coverUrl: true, duration: true},
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

// 下架歌曲（含文件清理）
export async function deleteSong(songId: number, singerId: number) {
    const song = await prisma.song.findUnique({
        where: {id: songId},
        select: {singerId: true, playUrl: true, coverUrl: true},
    });
    if (!song) {
        throw new NotFoundError(SongErrorMessage.NOT_FOUND);
    }
    if (song.singerId !== singerId) {
        throw new ForbiddenError(SongErrorMessage.NOT_OWNER);
    }

    // 删 DB 记录（级联删评论和歌单关联）
    await prisma.song.delete({where: {id: songId}});

    // 清理本地文件（不阻塞，失败忽略）
    if (song.playUrl?.startsWith('/static/')) {
        fs.unlink(path.resolve(__dirname, '../../', song.playUrl.slice(1)), () => {});
    }
    if (song.coverUrl?.startsWith('/static/')) {
        fs.unlink(path.resolve(__dirname, '../../', song.coverUrl.slice(1)), () => {});
    }
}

// 收藏/取消收藏（toggle：有则删，无则增）
export async function toggleFavorite(userId: number, songId: number) {
    const song = await prisma.song.findUnique({
        where: {id: songId},
        select: {id: true},
    });
    if (!song) {
        throw new NotFoundError(SongErrorMessage.NOT_FOUND);
    }

    // 找用户的收藏歌单
    const favPlaylist = await prisma.playlist.findFirst({
        where: {userId, isFavorite: true},
        select: {id: true},
    });
    if (!favPlaylist) {
        // 极端情况：收藏歌单被手动删除则重建
        const newFav = await prisma.playlist.create({
            data: {userId, name: '我的收藏', isFavorite: true},
        });
        await prisma.playlistSong.create({
            data: {playlistId: newFav.id, songId},
        });
        return {favorited: true};
    }

    const existing = await prisma.playlistSong.findUnique({
        where: {playlistId_songId: {playlistId: favPlaylist.id, songId}},
    });

    if (existing) {
        await prisma.playlistSong.delete({
            where: {playlistId_songId: {playlistId: favPlaylist.id, songId}},
        });
        return {favorited: false};
    } else {
        await prisma.playlistSong.create({
            data: {playlistId: favPlaylist.id, songId},
        });
        return {favorited: true};
    }
}

// 获取用户收藏列表
export async function getUserFavorites(userId: number, page: number, pageSize: number) {
    const favPlaylist = await prisma.playlist.findFirst({
        where: {userId, isFavorite: true},
        select: {
            id: true,
            name: true,
            userId: true,
            playlistSongs: {
                select: {
                    addedAt: true,
                    song: {
                        select: {
                            id: true,
                            name: true,
                            singerName: true,
                            playUrl: true,
                            coverUrl: true,
                            duration: true,
                        },
                    },
                },
                orderBy: {addedAt: 'desc'},
            },
        },
    });

    if (!favPlaylist) {
        return {list: [], total: 0, page, page_size: pageSize};
    }

    const allSongs = favPlaylist.playlistSongs;
    const total = allSongs.length;
    const skip = (page - 1) * pageSize;
    const pagedSongs = allSongs.slice(skip, skip + pageSize);

    return {
        playlist_id: favPlaylist.id,
        playlist_name: favPlaylist.name,
        user_id: favPlaylist.userId,
        songs: pagedSongs.map(ps => ({
            song_id: ps.song.id,
            song_name: ps.song.name,
            singer_name: ps.song.singerName,
            play_url: ps.song.playUrl,
            cover_url: ps.song.coverUrl,
            duration: ps.song.duration,
            added_at: ps.addedAt,
        })),
        total,
        page,
        page_size: pageSize,
    };
}
