import {z} from 'zod';
import {registry} from '../config/openapi';
import {updateUserSchema} from '../validators/user.validator';

// 路径参数 id 的 schema
const userIdParam = z.object({
    id: z.string().describe('用户 ID'),
});

// ===== GET /users/:id =====
registry.registerPath({
    method: 'get',
    path: '/users/{id}',
    summary: '获取用户信息',
    description: '公开接口，无需登录即可查看用户公开资料',
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
                            data: {
                                type: 'object',
                                properties: {
                                    id: {type: 'integer'},
                                    username: {type: 'string'},
                                    nickname: {type: 'string', nullable: true},
                                    email: {type: 'string'},
                                    avatar: {type: 'string', nullable: true},
                                    gender: {type: 'integer'},
                                    signature: {type: 'string', nullable: true},
                                    createdAt: {type: 'string', format: 'date-time'},
                                },
                            },
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

// ===== PUT /users/:id =====
registry.registerPath({
    method: 'put',
    path: '/users/{id}',
    summary: '修改用户资料',
    description: '需登录认证，仅允许修改自己的资料',
    security: [{bearerAuth: []}],
    tags: ['用户'],
    request: {
        params: userIdParam,
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
            description: '更新成功',
        },
        400: {
            description: '参数校验失败',
        },
        401: {
            description: '未登录',
        },
        403: {
            description: '无权修改他人资料',
        },
        404: {
            description: '用户不存在',
        },
    },
});
