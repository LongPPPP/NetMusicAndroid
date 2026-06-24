import prisma from '../config/database';
import {PlaylistErrorMessage} from '../constants/errorString';
import {ConflictError, NotFoundError} from '../errors/AppError';
import {sanitize} from '../utils/sanitize';
import type {CreatePlaylistInput} from '../validators/playlist.validator';

// 获取用户收藏的歌单列表
export async function getPlaylistsByUser(userId: number) {
    const playlists = await prisma.playlist.findMany({
        where: {userId},
        select: {
            id: true,
            name: true,
            createdAt: true,
        },
        orderBy: {createdAt: 'desc'},
    });

    return {
        list: playlists.map(p => ({
            playlist_id: p.id,
            playlist_name: p.name,
        })),
    };
}

// 获取歌单详情（含歌曲列表）
export async function getPlaylistDetail(playlistId: number) {
    const playlist = await prisma.playlist.findUnique({
        where: {id: playlistId},
        select: {
            id: true,
            name: true,
            playlistSongs: {
                select: {
                    song: {
                        select: {
                            id: true,
                            name: true,
                            singerName: true,
                        },
                    },
                },
                orderBy: {addedAt: 'asc'},
            },
        },
    });

    if (!playlist) {
        throw new NotFoundError(PlaylistErrorMessage.NOT_FOUND);
    }

    return {
        playlist_id: playlist.id,
        playlist_name: playlist.name,
        songs: playlist.playlistSongs.map(ps => ({
            song_id: ps.song.id,
            song_name: ps.song.name,
            singer_name: ps.song.singerName,
        })),
    };
}

// 创建歌单
export async function createPlaylist(data: CreatePlaylistInput) {
    const name = sanitize(data.playlist_name);

    // 检查同一用户下歌单名是否已存在
    const existing = await prisma.playlist.findUnique({
        where: {userId_name: {userId: data.user_id, name}},
        select: {id: true},
    });
    if (existing) {
        throw new ConflictError(PlaylistErrorMessage.NAME_EXISTS);
    }

    const playlist = await prisma.playlist.create({
        data: {
            userId: data.user_id,
            name,
        },
    });

    return {
        playlist_id: playlist.id,
    };
}
