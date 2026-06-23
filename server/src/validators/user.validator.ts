import {z} from 'zod';

// 更新用户资料校验
export const updateUserSchema = z.object({
    nickname: z
        .string()
        .trim()
        .min(1, '昵称不能为空')
        .max(20, '昵称最多 20 个字符')
        .optional(),
    avatar: z
        .url('头像地址格式不正确')
        .optional(),
    gender: z
        .number()
        .int()
        .min(0, '性别值无效')
        .max(2, '性别值无效')
        .optional(),
    signature: z
        .string()
        .trim()
        .max(100, '个性签名最多 100 个字符')
        .optional(),
});

export type UpdateUserInput = z.infer<typeof updateUserSchema>;