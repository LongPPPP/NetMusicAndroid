import {z} from 'zod';

// 搜索歌曲
export const searchSongsSchema = z.object({
    keyword: z
        .string()
        .trim()
        .min(1, '搜索关键字不能为空')
        .max(50, '搜索关键字最多 50 个字符'),
    page: z.coerce.number().int().positive().optional().default(1),
    page_size: z.coerce.number().int().positive().max(100).optional().default(20),
});

// 搜索歌手
export const searchSingersSchema = z.object({
    keyword: z
        .string()
        .trim()
        .min(1, '搜索关键字不能为空')
        .max(50, '搜索关键字最多 50 个字符'),
    page: z.coerce.number().int().positive().optional().default(1),
    page_size: z.coerce.number().int().positive().max(100).optional().default(20),
});

// 搜索歌单
export const searchPlaylistsSchema = z.object({
    keyword: z
        .string()
        .trim()
        .min(1, '搜索关键字不能为空')
        .max(50, '搜索关键字最多 50 个字符'),
    page: z.coerce.number().int().positive().optional().default(1),
    page_size: z.coerce.number().int().positive().max(100).optional().default(20),
});

export type SearchSongsInput = z.infer<typeof searchSongsSchema>;
export type SearchSingersInput = z.infer<typeof searchSingersSchema>;
export type SearchPlaylistsInput = z.infer<typeof searchPlaylistsSchema>;
