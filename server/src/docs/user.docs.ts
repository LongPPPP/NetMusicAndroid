import {z} from 'zod';
import {registry} from '../config/openapi';
import {updateUserSchema} from '../validators/user.validator';

// 路径参数 id 的 schema
const userIdParam = z.object({
    userId: z.coerce.number().int().positive().describe('用户 ID'),
});

const errorResponse = {
    type: 'object' as const,
    properties: {
        code: {type: 'integer' as const, example: 400},
        message: {type: 'string' as const, example: '参数校验失败'},
        data: {type: 'null' as const},
    },
};

// 通用用户信息响应
const userResponseSchema = {
    type: 'object' as const,
    properties: {
        id: {type: 'integer' as const},
        username: {type: 'string' as const},
        email: {type: 'string' as const},
        avatar: {type: 'string' as const, nullable: true},
        signature: {type: 'string' as const, nullable: true},
        role: {type: 'string' as const, enum: ['USER', 'ARTIST'] as string[], example: 'USER' as const},
        createdAt: {type: 'string' as const, format: 'date-time' as const},
    },
    example: {
        id: 1,
        username: '小明',
        email: 'xiaoming@example.com',
        avatar: null,
        signature: '音乐是我的生命',
        role: 'USER',
        createdAt: '2024-01-15T08:30:00.000Z',
    },
};

// ===== GET /users/:id =====
registry.registerPath({
    method: 'get',
    path: '/users/{userId}',
    summary: '获取用户公开信息',
    description: '公开接口，无需登录即可查看用户资料',
    tags: ['用户'],
    request: {
        params: userIdParam,
    },
    responses: {
        200: {
            description: '返回用户信息',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 200},
                            data: userResponseSchema,
                        },
                    },
                },
            },
        },
        400: {
            description: '路径参数不合法（用户 ID 必须为正整数）',
            content: {
                'application/json': {
                    schema: errorResponse,
                },
            },
        },
        404: {
            description: '用户不存在',
        },
    },
});

// ===== GET /users/me =====
registry.registerPath({
    method: 'get',
    path: '/users/me',
    summary: '获取当前用户信息',
    description: '需登录认证，返回当前登录用户的完整信息',
    security: [{bearerAuth: []}],
    tags: ['用户'],
    responses: {
        200: {
            description: '返回当前用户信息',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 200},
                            data: userResponseSchema,
                        },
                    },
                },
            },
        },
        401: {
            description: '未登录',
        },
    },
});

// ===== PATCH /users/me =====
registry.registerPath({
    method: 'patch',
    path: '/users/me',
    summary: '统一修改用户信息',
    description: '通过数据字典方式修改用户信息，支持字段：avatar, signature, username, email',
    security: [{bearerAuth: []}],
    tags: ['用户'],
    request: {
        body: {
            content: {
                'application/json': {
                    schema: updateUserSchema,
                },
            },
        },
    },
    responses: {
        200: {
            description: '修改成功',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 200},
                            message: {type: 'string', example: '修改成功'},
                            data: userResponseSchema,
                        },
                    },
                },
            },
        },
        400: {description: '参数校验失败 / 不支持修改的字段'},
        401: {description: '未登录'},
    },
});
