import {z} from 'zod';

// 统一修改用户信息（数据字典：PATCH /users/me）
export const updateUserSchema = z.object({
    field: z.enum(['avatar', 'signature', 'username', 'email']),
    value: z.string().trim().min(1, 'value 不能为空'),
});

export type UpdateUserInput = z.infer<typeof updateUserSchema>;
