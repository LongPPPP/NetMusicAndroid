/**
 * 类型化错误类 — 自带 HTTP 状态码，告别字符串比对
 *
 * 使用方式：
 *   throw new ConflictError('用户名已被占用');
 *   throw new NotFoundError('用户不存在');
 *
 * 全局 errorHandler 通过 instanceof 识别后直接返回 err.statusCode + err.message。
 */

export class AppError extends Error {
  public readonly statusCode: number;

  constructor(message: string, statusCode: number = 500) {
    super(message);
    this.statusCode = statusCode;
    // 保证 instanceof 在 TS 编译后仍然可靠
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

/** 400 — 请求参数校验失败 */
export class ValidationError extends AppError {
  constructor(message: string) {
    super(message, 400);
  }
}

/** 401 — 未登录 / Token 无效 */
export class UnauthorizedError extends AppError {
  constructor(message: string) {
    super(message, 401);
  }
}

/** 403 — 无权操作 */
export class ForbiddenError extends AppError {
  constructor(message: string) {
    super(message, 403);
  }
}

/** 404 — 资源不存在 */
export class NotFoundError extends AppError {
  constructor(message: string) {
    super(message, 404);
  }
}

/** 409 — 资源冲突（用户名/邮箱已被占用等） */
export class ConflictError extends AppError {
  constructor(message: string) {
    super(message, 409);
  }
}