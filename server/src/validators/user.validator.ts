import {z} from 'zod';

// 修改用户名
export const updateUsernameSchema = z.object({
    username: z
        .string()
        .trim()
        .min(1, '用户名不能为空')
        .max(16, '用户名最多 16 个字符')
        .regex(/^[a-zA-Z0-9_一-龥]+$/, '用户名仅支持英文、数字、下划线和中文'),
});

// 修改头像
export const updateAvatarSchema = z.object({
    avatar: z
        .string()
        .trim()
        .min(1, '头像地址不能为空')
        .url('头像地址格式不正确'),
});

// 修改个性签名
export const updateSignatureSchema = z.object({
    signature: z
        .string()
        .trim()
        .max(100, '个性签名最多 100 个字符'),
});

// 修改邮箱
export const updateEmailSchema = z.object({
    email: z
        .string()
        .trim()
        .min(1, '邮箱不能为空')
        .email('邮箱格式不正确'),
});

export type UpdateUsernameInput = z.infer<typeof updateUsernameSchema>;
export type UpdateAvatarInput = z.infer<typeof updateAvatarSchema>;
export type UpdateSignatureInput = z.infer<typeof updateSignatureSchema>;
export type UpdateEmailInput = z.infer<typeof updateEmailSchema>;
