import {z} from 'zod';

// 发表评论
export const createCommentSchema = z.object({
    content: z
        .string()
        .trim()
        .min(1, '评论内容不能为空')
        .max(500, '评论内容最多 500 个字符'),
});

// 歌曲列表查询参数
export const getSongsSchema = z.object({
    page: z.coerce.number().int().positive().optional().default(1),
    page_size: z.coerce.number().int().positive().max(100).optional().default(20),
    singer_id: z.coerce.number().int().positive().optional(),
});

// 评论列表查询参数
export const getCommentsSchema = z.object({
    page: z.coerce.number().int().positive().optional().default(1),
    page_size: z.coerce.number().int().positive().max(100).optional().default(20),
});

export type CreateCommentInput = z.infer<typeof createCommentSchema>;
export type GetSongsInput = z.infer<typeof getSongsSchema>;
export type GetCommentsInput = z.infer<typeof getCommentsSchema>;
