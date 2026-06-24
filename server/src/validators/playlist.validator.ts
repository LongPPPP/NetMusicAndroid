import {z} from 'zod';

// 创建歌单
export const createPlaylistSchema = z.object({
    user_id: z.number().int().positive('用户 ID 不能为空'),
    playlist_name: z
        .string()
        .trim()
        .min(1, '歌单名称不能为空')
        .max(30, '歌单名称最多 30 个字符'),
});

// 获取歌单列表
export const getPlaylistSchema = z.object({
    user_id: z.string().min(1, '用户 ID 不能为空'),
});

export type CreatePlaylistInput = z.infer<typeof createPlaylistSchema>;
