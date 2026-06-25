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

// ===== GET /users/:userId/playlists =====
registry.registerPath({
    method: 'get',
    path: '/users/{userId}/playlists',
    summary: '获取指定用户的歌单列表',
    description: '公开接口，无需登录即可查看用户的歌单',
    tags: ['用户', '歌单'],
    request: {params: userIdParam},
    responses: {
        200: {
            description: '返回用户的歌单列表',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 200},
                            data: {
                                type: 'object',
                                properties: {
                                    list: {
                                        type: 'array',
                                        items: {
                                            type: 'object',
                                            properties: {
                                                playlist_id: {type: 'integer'},
                                                playlist_name: {type: 'string'},
                                                song_count: {type: 'integer', description: '歌内歌曲数量'},
                                                created_at: {type: 'string', format: 'date-time'},
                                            },
                                        },
                                    },
                                },
                                example: {
                                    list: [
                                        {playlist_id: 1, playlist_name: '我的最爱', song_count: 15, created_at: '2024-01-15T08:30:00.000Z'},
                                        {playlist_id: 2, playlist_name: '跑步歌单', song_count: 8, created_at: '2024-02-20T12:00:00.000Z'},
                                    ],
                                },
                            },
                        },
                    },
                },
            },
        },
        400: {description: '参数校验失败（用户 ID 必须为正整数）'},
        404: {description: '用户不存在'},
    },
});

// ===== GET /users/me/playlists =====
registry.registerPath({
    method: 'get',
    path: '/users/me/playlists',
    summary: '获取我的歌单列表',
    description: '需登录认证，返回当前登录用户的歌单列表',
    security: [{bearerAuth: []}],
    tags: ['用户', '歌单'],
    responses: {
        200: {
            description: '返回当前用户的歌单列表',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 200},
                            data: {
                                type: 'object',
                                properties: {
                                    list: {
                                        type: 'array',
                                        items: {
                                            type: 'object',
                                            properties: {
                                                playlist_id: {type: 'integer'},
                                                playlist_name: {type: 'string'},
                                                song_count: {type: 'integer', description: '歌内歌曲数量'},
                                                created_at: {type: 'string', format: 'date-time'},
                                            },
                                        },
                                    },
                                },
                                example: {
                                    list: [
                                        {playlist_id: 1, playlist_name: '我的最爱', song_count: 15, created_at: '2024-01-15T08:30:00.000Z'},
                                    ],
                                },
                            },
                        },
                    },
                },
            },
        },
        401: {description: '未登录'},
    },
});

// ===== GET /users/me/comments =====
registry.registerPath({
    method: 'get',
    path: '/users/me/comments',
    summary: '获取我的所有评论',
    description: '需登录认证，分页返回当前用户发表过的所有评论（按时间倒序）',
    security: [{bearerAuth: []}],
    tags: ['用户'],
    request: {
        query: z.object({
            page: z.coerce.number().int().positive().optional().describe('页码，默认 1'),
            page_size: z.coerce.number().int().positive().max(100).optional().describe('每页数量，默认 20'),
        }),
    },
    responses: {
        200: {
            description: '返回当前用户的评论列表',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 200},
                            data: {
                                type: 'object',
                                properties: {
                                    list: {
                                        type: 'array',
                                        items: {
                                            type: 'object',
                                            properties: {
                                                comment_id: {type: 'integer'},
                                                content: {type: 'string'},
                                                created_at: {type: 'string', format: 'date-time'},
                                                song: {
                                                    type: 'object',
                                                    properties: {
                                                        song_id: {type: 'integer'},
                                                        song_name: {type: 'string'},
                                                    },
                                                },
                                            },
                                        },
                                    },
                                    total: {type: 'integer'},
                                    page: {type: 'integer'},
                                    page_size: {type: 'integer'},
                                },
                                example: {
                                    list: [
                                        {comment_id: 1, content: '这首歌太好听了', created_at: '2024-01-15T08:30:00.000Z', song: {song_id: 1, song_name: '七里香'}},
                                    ],
                                    total: 1,
                                    page: 1,
                                    page_size: 20,
                                },
                            },
                        },
                    },
                },
            },
        },
        401: {description: '未登录'},
    },
});

// ===== PUT /users/me/avatar =====
registry.registerPath({
    method: 'put',
    path: '/users/me/avatar',
    summary: '上传/替换头像',
    description: '上传新头像图片，自动替换用户头像（旧本地文件将被清理）。支持的格式：JPG/PNG/GIF/WebP，最大 5MB',
    security: [{bearerAuth: []}],
    tags: ['用户'],
    request: {
        body: {
            content: {
                'multipart/form-data': {
                    schema: {
                        type: 'object',
                        properties: {
                            file: {
                                type: 'string',
                                format: 'binary',
                                description: '头像图片文件',
                            },
                        },
                        required: ['file'],
                    },
                },
            },
        },
    },
    responses: {
        200: {
            description: '上传成功',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 200},
                            message: {type: 'string', example: '上传成功'},
                            data: {
                                type: 'object',
                                properties: {
                                    url: {type: 'string', example: '/static/avatars/uuid.jpg'},
                                },
                            },
                        },
                    },
                },
            },
        },
        400: {description: '未选择文件 / 文件格式不支持'},
        401: {description: '未登录'},
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
