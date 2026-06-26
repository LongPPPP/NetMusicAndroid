import prisma from '../config/database';
import {PlaylistErrorMessage} from '../constants/errorString';
import {ConflictError, ForbiddenError, NotFoundError} from '../errors/AppError';
import {sanitize} from '../utils/sanitize';
import type {CreatePlaylistInput, UpdatePlaylistInput} from '../validators/playlist.validator';

// 获取用户收藏的歌单列表
export async function getPlaylistsByUser(userId: number) {
    const playlists = await prisma.playlist.findMany({
        where: {userId},
        select: {
            id: true,
            name: true,
            createdAt: true,
            _count: {select: {playlistSongs: true}},
        },
        orderBy: {createdAt: 'desc'},
    });

    return {
        list: playlists.map(p => ({
            playlist_id: p.id,
            playlist_name: p.name,
            song_count: p._count.playlistSongs,
            created_at: p.createdAt,
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
                        select: {id: true, name: true, singerName: true},
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
export async function createPlaylist(userId: number, data: CreatePlaylistInput) {
    const name = sanitize(data.name);

    const existing = await prisma.playlist.findUnique({
        where: {userId_name: {userId, name}},
        select: {id: true},
    });
    if (existing) {
        throw new ConflictError(PlaylistErrorMessage.NAME_EXISTS);
    }

    const playlist = await prisma.playlist.create({data: {userId, name}});
    return {playlist_id: playlist.id};
}

// 校验歌单归属，同时返回 isFavorite 供调用方判断
async function assertPlaylistOwner(playlistId: number, userId: number) {
    const playlist = await prisma.playlist.findUnique({
        where: {id: playlistId},
        select: {userId: true, isFavorite: true},
    });
    if (!playlist) {
        throw new NotFoundError(PlaylistErrorMessage.NOT_FOUND);
    }
    if (playlist.userId !== userId) {
        throw new ForbiddenError('无权操作此歌单');
    }
    return playlist;
}

// 歌单添加歌曲
export async function addSongToPlaylist(playlistId: number, songId: number, userId: number) {
    await assertPlaylistOwner(playlistId, userId);

    const song = await prisma.song.findUnique({
        where: {id: songId},
        select: {id: true},
    });
    if (!song) {
        throw new NotFoundError('歌曲不存在');
    }

    const existing = await prisma.playlistSong.findUnique({
        where: {playlistId_songId: {playlistId, songId}},
    });
    if (existing) {
        throw new ConflictError('歌曲已在歌单中');
    }

    await prisma.playlistSong.create({data: {playlistId, songId}});
}

// 歌单移除歌曲
export async function removeSongFromPlaylist(playlistId: number, songId: number, userId: number) {
    await assertPlaylistOwner(playlistId, userId);

    const link = await prisma.playlistSong.findUnique({
        where: {playlistId_songId: {playlistId, songId}},
    });
    if (!link) {
        throw new NotFoundError('歌曲不在该歌单中');
    }

    await prisma.playlistSong.delete({
        where: {playlistId_songId: {playlistId, songId}},
    });
}

// 重命名歌单
export async function renamePlaylist(playlistId: number, userId: number, data: UpdatePlaylistInput) {
    const pl = await assertPlaylistOwner(playlistId, userId);
    if (pl.isFavorite) {
        throw new ForbiddenError(PlaylistErrorMessage.FAVORITE_PROTECTED);
    }

    const name = sanitize(data.name);

    // 检查同一用户下是否有重名歌单
    const existing = await prisma.playlist.findUnique({
        where: {userId_name: {userId, name}},
        select: {id: true},
    });
    if (existing && existing.id !== playlistId) {
        throw new ConflictError(PlaylistErrorMessage.NAME_EXISTS);
    }

    const playlist = await prisma.playlist.update({
        where: {id: playlistId},
        data: {name},
        select: {
            id: true,
            name: true,
            createdAt: true,
            _count: {select: {playlistSongs: true}},
        },
    });

    return {
        playlist_id: playlist.id,
        playlist_name: playlist.name,
        song_count: playlist._count.playlistSongs,
        created_at: playlist.createdAt,
    };
}

// 删除歌单
export async function deletePlaylist(playlistId: number, userId: number) {
    const pl = await assertPlaylistOwner(playlistId, userId);
    if (pl.isFavorite) {
        throw new ForbiddenError(PlaylistErrorMessage.FAVORITE_PROTECTED);
    }

    await prisma.playlist.delete({where: {id: playlistId}});
}
