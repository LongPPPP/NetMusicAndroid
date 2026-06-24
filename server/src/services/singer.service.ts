import prisma from '../config/database';
import {SingerErrorMessage} from '../constants/errorString';
import {NotFoundError} from '../errors/AppError';

// 获取歌手详情（含热门歌曲）
export async function getSingerDetail(singerId: number) {
    const singer = await prisma.singer.findUnique({
        where: {id: singerId},
        select: {
            id: true,
            name: true,
            avatarUrl: true,
            description: true,
            songs: {
                select: {
                    id: true,
                    name: true,
                },
                orderBy: {id: 'asc'},
                take: 10, // 热门歌曲取前 10 首
            },
        },
    });

    if (!singer) {
        throw new NotFoundError(SingerErrorMessage.NOT_FOUND);
    }

    return {
        singer_id: singer.id,
        singer_name: singer.name,
        avatar_url: singer.avatarUrl,
        description: singer.description,
        hot_songs: singer.songs.map(s => ({
            song_id: s.id,
        })),
    };
}
