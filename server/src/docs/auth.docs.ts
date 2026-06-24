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
                                    accessToken: {type: 'string', example: 'eyJhbGciOiJIUzI1NiIs...'},
                                    refreshToken: {type: 'string', example: 'eyJhbGciOiJIUzI1NiIs...'},
                                    user: {
                                        type: 'object',
                                        properties: {
                                            id: {type: 'integer'},
                                            username: {type: 'string'},
                                            email: {type: 'string'},
                                            avatar: {type: 'string', nullable: true},
                                            gender: {type: 'string', enum: ['UNKNOWN', 'MALE', 'FEMALE']},
                                            signature: {type: 'string', nullable: true},
                                            role: {type: 'string', enum: ['USER', 'ARTIST'], example: 'USER'},
                                        },
                                    },
                                },
                            },
                        },
                    },
                },
            },
        },
        400: {
            description: '参数校验失败 / 邮箱已被占用',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 400},
                            message: {type: 'string', example: '邮箱已被注册'},
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
                                    userId: {type: 'integer', example: 1},
                                    accessToken: {type: 'string', example: 'eyJhbGciOiJIUzI1NiIs...'},
                                    refreshToken: {type: 'string', example: 'eyJhbGciOiJIUzI1NiIs...'},
                                    user: {
                                        type: 'object',
                                        properties: {
                                            id: {type: 'integer'},
                                            username: {type: 'string'},
                                            email: {type: 'string'},
                                            avatar: {type: 'string', nullable: true},
                                            gender: {type: 'string', enum: ['UNKNOWN', 'MALE', 'FEMALE']},
                                            signature: {type: 'string', nullable: true},
                                            role: {type: 'string', enum: ['USER', 'ARTIST'], example: 'USER'},
                                        },
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
                                    accessToken: {type: 'string', example: 'eyJhbGciOiJIUzI1NiIs...'},
                                    expiresIn: {type: 'integer', example: 900},
                                },
                            },
                        },
                    },
                },
            },
        },
        401: {
            description: 'Refresh Token 无效或已过期',
        },
    },
});
