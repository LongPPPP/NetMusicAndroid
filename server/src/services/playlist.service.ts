import prisma from '../config/database';
import {PlaylistErrorMessage} from '../constants/errorString';
import {ForbiddenError, NotFoundError} from '../errors/AppError';
import {prismaError} from '../utils/prisma';
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
            playlistSongs: {
                select: {song: {select: {coverUrl: true}}},
                orderBy: {addedAt: 'asc'},
                take: 5,
            },
        },
        orderBy: {createdAt: 'desc'},
    });

    return {
        list: playlists.map(p => ({
            playlist_id: p.id,
            playlist_name: p.name,
            song_count: p._count.playlistSongs,
            cover_url: p.playlistSongs.map(ps => ps.song.coverUrl).find(url => url) ?? null,
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
            userId: true,
            createdAt: true,
            playlistSongs: {
                select: {
                    addedAt: true,
                    song: {
                        select: {id: true, name: true, singerName: true, coverUrl: true, playUrl: true, duration: true},
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
        user_id: playlist.userId,
        cover_url: playlist.playlistSongs.map(ps => ps.song.coverUrl).find(url => url) ?? null,
        created_at: playlist.createdAt,
        songs: playlist.playlistSongs.map(ps => ({
            song_id: ps.song.id,
            song_name: ps.song.name,
            singer_name: ps.song.singerName,
            cover_url: ps.song.coverUrl,
            play_url: ps.song.playUrl,
            duration: ps.song.duration,
            added_at: ps.addedAt,
        })),
    };
}

// 创建歌单
export async function createPlaylist(userId: number, data: CreatePlaylistInput) {
    const name = sanitize(data.name);

    const playlist = await prisma.playlist.create({data: {userId, name}})
        .catch(prismaError({P2002: PlaylistErrorMessage.NAME_EXISTS}));
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

    await prisma.playlistSong.create({data: {playlistId, songId}})
        .catch(prismaError({P2002: '歌曲已在歌单中'}));
}

// 歌单移除歌曲
export async function removeSongFromPlaylist(playlistId: number, songId: number, userId: number) {
    await assertPlaylistOwner(playlistId, userId);

    await prisma.playlistSong.delete({
        where: {playlistId_songId: {playlistId, songId}},
    }).catch(prismaError({P2025: '歌曲不在该歌单中'}));
}

// 重命名歌单
export async function renamePlaylist(playlistId: number, userId: number, data: UpdatePlaylistInput) {
    const pl = await assertPlaylistOwner(playlistId, userId);
    if (pl.isFavorite) {
        throw new ForbiddenError(PlaylistErrorMessage.FAVORITE_PROTECTED);
    }

    const name = sanitize(data.name);

    const playlist = await prisma.playlist.update({
        where: {id: playlistId},
        data: {name},
        select: {
            id: true,
            name: true,
            createdAt: true,
            _count: {select: {playlistSongs: true}},
            playlistSongs: {
                select: {song: {select: {coverUrl: true}}},
                orderBy: {addedAt: 'asc'},
                take: 5,
            },
        },
    }).catch(prismaError({
        P2002: PlaylistErrorMessage.NAME_EXISTS,
        P2025: PlaylistErrorMessage.NOT_FOUND,
    }));

    return {
        playlist_id: playlist.id,
        playlist_name: playlist.name,
        song_count: playlist._count.playlistSongs,
        cover_url: playlist.playlistSongs.map(ps => ps.song.coverUrl).find(url => url) ?? null,
        created_at: playlist.createdAt,
    };
}

// 删除歌单
export async function deletePlaylist(playlistId: number, userId: number) {
    const pl = await assertPlaylistOwner(playlistId, userId);
    if (pl.isFavorite) {
        throw new ForbiddenError(PlaylistErrorMessage.FAVORITE_PROTECTED);
    }

    await prisma.playlist.delete({where: {id: playlistId}})
        .catch(prismaError({P2025: PlaylistErrorMessage.NOT_FOUND}));
}
