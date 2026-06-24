import prisma from '../config/database';

// 搜索歌曲（按歌名或歌手名模糊匹配）
export async function searchSongs(keyword: string) {
    const songs = await prisma.song.findMany({
        where: {
            OR: [
                {name: {contains: keyword}},
                {singerName: {contains: keyword}},
            ],
        },
        select: {
            id: true,
            name: true,
            singerName: true,
        },
        orderBy: {id: 'asc'},
    });

    return {
        list: songs.map(s => ({
            song_id: s.id,
            song_name: s.name,
            singer_name: s.singerName,
        })),
    };
}
