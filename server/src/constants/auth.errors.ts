/**
 * 认证模块业务错误码
 * 使用枚举而非字符串常量，天然具备类型安全
 */
export enum AuthError {
    USERNAME_EXISTS = 'USERNAME_EXISTS',
    EMAIL_EXISTS = 'EMAIL_EXISTS',
    USER_NOT_FOUND = 'USER_NOT_FOUND',
    WRONG_PASSWORD = 'WRONG_PASSWORD',
}
