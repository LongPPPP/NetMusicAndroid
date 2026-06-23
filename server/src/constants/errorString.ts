/** 用户提示消息，统一管理消除魔法字符串 */
export const AuthErrorMessage = {
    USERNAME_EXISTS: '用户名已被占用',
    EMAIL_EXISTS: '邮箱已被注册',
    USER_NOT_FOUND: '用户不存在',
    LOGIN_FAILED: '用户名或密码错误',
    REGISTER_FAILED: '注册失败',
    LOGIN_ERROR: '登录失败',
    TOKEN_INVALID: 'Token 验证失败',
} as const;
