import {z} from 'zod';

// 发表评论
export const createCommentSchema = z.object({
    user_id: z.number().int().positive('用户 ID 不能为空'),
    content: z
        .string()
        .trim()
        .min(1, '评论内容不能为空')
        .max(500, '评论内容最多 500 个字符'),
});

export type CreateCommentInput = z.infer<typeof createCommentSchema>;
