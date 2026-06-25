import {z} from 'zod';

// 歌手列表查询参数
export const getSingersSchema = z.object({
    page: z.coerce.number().int().positive().optional().default(1),
    page_size: z.coerce.number().int().positive().max(100).optional().default(20),
});

export type GetSingersInput = z.infer<typeof getSingersSchema>;
