import prisma from '../config/database';

// 搜索歌曲
export async function searchSongs(keyword: string, page: number, page_size: number) {
    const where = {
        OR: [
            {name: {contains: keyword}},
            {singerName: {contains: keyword}},
        ],
    };

    const skip = (page - 1) * page_size;

    const [songs, total] = await Promise.all([
        prisma.song.findMany({
            where,
            select: {id: true, name: true, singerId: true, singerName: true, coverUrl: true},
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
            singer_id: s.singerId,
            singer_name: s.singerName,
            cover_url: s.coverUrl,
        })),
        total,
        page,
        page_size,
    };
}

// 搜索歌手
export async function searchSingers(keyword: string, page: number, page_size: number) {
    const where = {name: {contains: keyword}};

    const skip = (page - 1) * page_size;

    const [singers, total] = await Promise.all([
        prisma.singer.findMany({
            where,
            select: {id: true, name: true, avatarUrl: true},
            skip,
            take: page_size,
            orderBy: {id: 'asc'},
        }),
        prisma.singer.count({where}),
    ]);

    return {
        list: singers.map(s => ({
            singer_id: s.id,
            singer_name: s.name,
            avatar_url: s.avatarUrl,
        })),
        total,
        page,
        page_size,
    };
}

// 搜索歌单
export async function searchPlaylists(keyword: string, page: number, page_size: number) {
    const where = {name: {contains: keyword}};

    const skip = (page - 1) * page_size;

    const [playlists, total] = await Promise.all([
        prisma.playlist.findMany({
            where,
            select: {
                id: true,
                name: true,
                _count: {select: {playlistSongs: true}},
                playlistSongs: {
                    select: {song: {select: {coverUrl: true}}},
                    orderBy: {addedAt: 'asc'},
                    take: 5,
                },
            },
            skip,
            take: page_size,
            orderBy: {id: 'asc'},
        }),
        prisma.playlist.count({where}),
    ]);

    return {
        list: playlists.map(p => ({
            playlist_id: p.id,
            playlist_name: p.name,
            song_count: p._count.playlistSongs,
            cover_url: p.playlistSongs.map(ps => ps.song.coverUrl).find(url => url) ?? null,
        })),
        total,
        page,
        page_size,
    };
}
