import {z} from 'zod';

// 创建歌单
export const createPlaylistSchema = z.object({
    name: z
        .string()
        .trim()
        .min(1, '歌单名称不能为空')
        .max(30, '歌单名称最多 30 个字符'),
});

// 重命名歌单
export const updatePlaylistSchema = z.object({
    name: z
        .string()
        .trim()
        .min(1, '歌单名称不能为空')
        .max(30, '歌单名称最多 30 个字符'),
});

// 歌单加歌
export const addPlaylistSongSchema = z.object({
    song_id: z.number().int().positive('歌曲 ID 不能为空'),
});

export type CreatePlaylistInput = z.infer<typeof createPlaylistSchema>;
export type UpdatePlaylistInput = z.infer<typeof updatePlaylistSchema>;
export type AddPlaylistSongInput = z.infer<typeof addPlaylistSongSchema>;
