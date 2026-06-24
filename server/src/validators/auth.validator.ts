import {z} from 'zod';

// 注册参数校验
export const registerSchema = z.object({
    username: z
        .string()
        .trim()
        .min(4, '用户名至少 4 个字符')
        .max(16, '用户名最多 16 个字符')
        .regex(/^[a-zA-Z0-9_]+$/, '用户名仅支持英文、数字和下划线'),
    password: z
        .string()
        .min(6, '密码至少 6 个字符')
        .max(20, '密码最多 20 个字符')
        .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d!@#$%^&*()_\-+.+=]{6,20}$/,
            '密码必须包含大小写字母、数字，可使用 !@#$%^&*()_.-+=')
        .refine(pwd => !/\s/.test(pwd), '密码不能包含空格'),
    confirmPassword: z
        .string()
        .min(1, '确认密码不能为空'),
    email: z
        .email('邮箱格式不正确'),
}).refine(data => data.password === data.confirmPassword, {
    message: '两次密码不一致',
    path: ['confirmPassword'],
});

// 登录参数校验（使用邮箱登录）
export const loginSchema = z.object({
    email: z
        .string()
        .trim()
        .min(1, '邮箱不能为空')
        .email('邮箱格式不正确'),
    password: z
        .string()
        .min(1, '密码不能为空'),
});

// 从 schema 推导 TypeScript 类型
export type RegisterInput = z.infer<typeof registerSchema>;
export type LoginInput = z.infer<typeof loginSchema>;

// Refresh Token 校验
export const refreshTokenSchema = z.object({
    refreshToken: z.string().min(1, 'refreshToken 不能为空'),
});
export type RefreshTokenInput = z.infer<typeof refreshTokenSchema>;