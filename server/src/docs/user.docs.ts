import {z} from 'zod';
import {registry} from '../config/openapi';
import {
    updateAvatarSchema,
    updateEmailSchema,
    updateSignatureSchema,
    updateUsernameSchema,
} from '../validators/user.validator';

// 路径参数 id 的 schema
const userIdParam = z.object({
    id: z.string().describe('用户 ID'),
});

// 通用用户信息响应
const userResponseSchema = {
    type: 'object',
    properties: {
        id: {type: 'integer'},
        username: {type: 'string'},
        email: {type: 'string'},
        avatar: {type: 'string', nullable: true},
        gender: {type: 'string', enum: ['UNKNOWN', 'MALE', 'FEMALE'] as string[]},
        signature: {type: 'string', nullable: true},
        role: {type: 'string', enum: ['USER', 'ARTIST'] as string[], example: 'USER'},
        createdAt: {type: 'string', format: 'date-time'},
    },
} as const;

// ===== GET /users/:id =====
registry.registerPath({
    method: 'get',
    path: '/users/{id}',
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

// ===== PATCH /users/me/username =====
registry.registerPath({
    method: 'patch',
    path: '/users/me/username',
    summary: '修改用户名',
    security: [{bearerAuth: []}],
    tags: ['用户'],
    request: {
        body: {
            content: {
                'application/json': {
                    schema: updateUsernameSchema,
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
                            message: {type: 'string', example: '用户名修改成功'},
                            data: userResponseSchema,
                        },
                    },
                },
            },
        },
        400: {description: '参数校验失败'},
        401: {description: '未登录'},
    },
});

// ===== PATCH /users/me/avatar =====
registry.registerPath({
    method: 'patch',
    path: '/users/me/avatar',
    summary: '修改头像',
    security: [{bearerAuth: []}],
    tags: ['用户'],
    request: {
        body: {
            content: {
                'application/json': {
                    schema: updateAvatarSchema,
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
                            message: {type: 'string', example: '头像修改成功'},
                            data: userResponseSchema,
                        },
                    },
                },
            },
        },
        400: {description: '参数校验失败'},
        401: {description: '未登录'},
    },
});

// ===== PATCH /users/me/signature =====
registry.registerPath({
    method: 'patch',
    path: '/users/me/signature',
    summary: '修改个性签名',
    security: [{bearerAuth: []}],
    tags: ['用户'],
    request: {
        body: {
            content: {
                'application/json': {
                    schema: updateSignatureSchema,
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
                            message: {type: 'string', example: '个性签名修改成功'},
                            data: userResponseSchema,
                        },
                    },
                },
            },
        },
        400: {description: '参数校验失败'},
        401: {description: '未登录'},
    },
});

// ===== PATCH /users/me/email =====
registry.registerPath({
    method: 'patch',
    path: '/users/me/email',
    summary: '修改邮箱',
    security: [{bearerAuth: []}],
    tags: ['用户'],
    request: {
        body: {
            content: {
                'application/json': {
                    schema: updateEmailSchema,
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
                            message: {type: 'string', example: '邮箱修改成功'},
                            data: userResponseSchema,
                        },
                    },
                },
            },
        },
        400: {description: '参数校验失败 / 邮箱已被占用'},
        401: {description: '未登录'},
        409: {description: '邮箱已被其他用户占用'},
    },
});
