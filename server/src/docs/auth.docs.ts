import {registry} from '../config/openapi';
import {loginSchema, registerSchema} from '../validators/auth.validator';

const errorResponse = {
    type: 'object' as const,
    properties: {
        code: {type: 'integer' as const, example: 400},
        message: {type: 'string' as const, example: '参数校验失败'},
        data: {type: 'null' as const},
    },
};

// ===== POST /auth/register =====
registry.registerPath({
    method: 'post',
    path: '/auth/register',
    summary: '用户注册',
    tags: ['认证'],
    request: {
        body: {
            content: {
                'application/json': {
                    schema: registerSchema,
                },
            },
        },
    },
    responses: {
        201: {
            description: '注册成功',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 201},
                            message: {type: 'string', example: '注册成功'},
                            data: {type: 'null'},
                        },
                    },
                },
            },
        },
        400: {
            description: '参数校验失败 / 邮箱已被占用',
            content: {
                'application/json': {
                    schema: errorResponse,
                },
            },
        },
        429: {
            description: '请求过于频繁（10次/15分钟）',
        },
    },
});

// ===== POST /auth/login =====
registry.registerPath({
    method: 'post',
    path: '/auth/login',
    summary: '用户登录（邮箱+密码）',
    tags: ['认证'],
    request: {
        body: {
            content: {
                'application/json': {
                    schema: loginSchema,
                },
            },
        },
    },
    responses: {
        200: {
            description: '登录成功',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 200},
                            message: {type: 'string', example: '登录成功'},
                            data: {
                                type: 'object',
                                properties: {
                                    user_id: {type: 'integer', example: 1},
                                    access_token: {type: 'string', example: 'eyJhbGciOiJIUzI1NiIs...'},
                                    expires_in: {type: 'integer', example: 900, description: 'Access Token 有效期（秒）'},
                                    refresh_token: {type: 'string', example: 'eyJhbGciOiJIUzI1NiIs...'},
                                    refresh_expires_in: {type: 'integer', example: 604800, description: 'Refresh Token 有效期（秒）'},
                                    user: {
                                        type: 'object',
                                        properties: {
                                            id: {type: 'integer'},
                                            username: {type: 'string'},
                                            email: {type: 'string'},
                                            avatar: {type: 'string', nullable: true},
                                            signature: {type: 'string', nullable: true},
                                            role: {type: 'string', enum: ['USER', 'ARTIST'], example: 'USER'},
                                            comment_count: {type: 'integer', description: '评论数量'},
                                            favorite_count: {type: 'integer', description: '收藏歌曲数量'},
                                        },
                                        example: {
                                            id: 1,
                                            username: '小明',
                                            email: 'xiaoming@example.com',
                                            avatar: null,
                                            signature: '音乐是我的生命',
                                            role: 'USER',
                                            comment_count: 12,
                                            favorite_count: 5,
                                        },
                                    },
                                },
                                example: {
                                    user_id: 1,
                                    access_token: 'eyJhbGciOiJIUzI1NiIs...',
                                    expires_in: 900,
                                    refresh_token: 'eyJhbGciOiJIUzI1NiIs...',
                                    refresh_expires_in: 604800,
                                    user: {
                                        id: 1,
                                        username: '小明',
                                        email: 'xiaoming@example.com',
                                        avatar: null,
                                        signature: '音乐是我的生命',
                                        role: 'USER',
                                        comment_count: 12,
                                        favorite_count: 5,
                                    },
                                },
                            },
                        },
                    },
                },
            },
        },
        400: {
            description: '邮箱或密码错误',
            content: {
                'application/json': {
                    schema: errorResponse,
                },
            },
        },
        429: {
            description: '请求过于频繁（10次/15分钟）',
        },
    },
});

// ===== POST /auth/refresh =====
registry.registerPath({
    method: 'post',
    path: '/auth/refresh',
    summary: '刷新 Access Token',
    description: '使用 Refresh Token 获取新的 Access Token，无需重新登录',
    tags: ['认证'],
    request: {
        body: {
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            refreshToken: {type: 'string', description: '登录/注册时获取的 Refresh Token'},
                        },
                        required: ['refreshToken'],
                    },
                },
            },
        },
    },
    responses: {
        200: {
            description: '刷新成功',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 200},
                            message: {type: 'string', example: 'Token 刷新成功'},
                            data: {
                                type: 'object',
                                properties: {
                                    access_token: {type: 'string', example: 'eyJhbGciOiJIUzI1NiIs...'},
                                    expires_in: {type: 'integer', example: 900},
                                },
                            },
                        },
                    },
                },
            },
        },
        400: {
            description: '参数校验失败（refreshToken 不能为空）',
            content: {
                'application/json': {
                    schema: errorResponse,
                },
            },
        },
        401: {
            description: 'Refresh Token 无效或已过期',
        },
    },
});
