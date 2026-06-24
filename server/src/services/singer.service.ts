import prisma from '../config/database';
import {SingerErrorMessage} from '../constants/errorString';
import {NotFoundError} from '../errors/AppError';
import type {GetSingersInput} from '../validators/singer.validator';

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
                select: {id: true, name: true},
                orderBy: {id: 'asc'},
                take: 10,
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
        hot_songs: singer.songs.map(s => ({song_id: s.id, song_name: s.name})),
    };
}

// 分页获取歌手列表
export async function listSingers(params: GetSingersInput) {
    const {page, page_size} = params;
    const skip = (page - 1) * page_size;

    const [singers, total] = await Promise.all([
        prisma.singer.findMany({
            select: {
                id: true,
                name: true,
                avatarUrl: true,
            },
            skip,
            take: page_size,
            orderBy: {id: 'asc'},
        }),
        prisma.singer.count(),
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
