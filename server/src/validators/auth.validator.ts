import { z } from 'zod';

// 注册参数校验
export const registerSchema = z.object({
  username: z
    .string()
    .trim()
    .min(3, '用户名至少 3 个字符')
    .max(20, '用户名最多 20 个字符'),
  password: z
    .string()
    .min(6, '密码至少 6 个字符')
    .max(50, '密码最多 50 个字符'),
  email: z
    .string()
    .email('邮箱格式不正确'),
});

// 登录参数校验
export const loginSchema = z.object({
  username: z
    .string()
    .trim()
    .min(1, '用户名不能为空'),
  password: z
    .string()
    .min(1, '密码不能为空'),
});

// 从 schema 推导 TypeScript 类型
export type RegisterInput = z.infer<typeof registerSchema>;
export type LoginInput = z.infer<typeof loginSchema>;