import {registry} from '../config/openapi';
import {loginSchema, registerSchema} from '../validators/auth.validator';

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
                            data: {
                                type: 'object',
                                properties: {
                                    userId: {type: 'integer', example: 1},
                                    token: {type: 'string', example: 'eyJhbGciOiJIUzI1NiIs...'},
                                },
                            },
                        },
                    },
                },
            },
        },
        400: {
            description: '参数校验失败 / 用户名或邮箱已被占用',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 400},
                            message: {type: 'string', example: '用户名已被占用'},
                            data: {type: 'null'},
                        },
                    },
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
    summary: '用户登录',
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
                                    userId: {type: 'integer', example: 1},
                                    token: {type: 'string', example: 'eyJhbGciOiJIUzI1NiIs...'},
                                    expiresIn: {type: 'integer', example: 604800},
                                },
                            },
                        },
                    },
                },
            },
        },
        400: {
            description: '用户名或密码错误',
        },
        429: {
            description: '请求过于频繁（10次/15分钟）',
        },
    },
});

// ===== POST /auth/verify-token =====
registry.registerPath({
    method: 'post',
    path: '/auth/verify-token',
    summary: '验证 Token（自动登录）',
    description: '客户端启动时携带本地 token 验证有效性，返回用户基本信息',
    security: [{bearerAuth: []}],
    tags: ['认证'],
    responses: {
        200: {
            description: 'Token 有效',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 200},
                            message: {type: 'string', example: 'Token 有效'},
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
                                },
                            },
                        },
                    },
                },
            },
        },
        401: {
            description: 'Token 无效或已过期',
        },
    },
});
