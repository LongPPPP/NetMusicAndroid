import {z} from 'zod';

export const searchSchema = z.object({
    keyword: z
        .string()
        .trim()
        .min(1, '搜索关键字不能为空')
        .max(50, '搜索关键字最多 50 个字符'),
});

export type SearchInput = z.infer<typeof searchSchema>;
