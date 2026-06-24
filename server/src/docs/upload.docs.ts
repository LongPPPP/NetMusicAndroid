import {registry} from '../config/openapi';

const errorResponse = {
    type: 'object' as const,
    properties: {
        code: {type: 'integer' as const, example: 400},
        message: {type: 'string' as const, example: '参数校验失败'},
        data: {type: 'null' as const},
    },
};

// ===== POST /upload/avatar =====
registry.registerPath({
    method: 'post',
    path: '/upload/avatar',
    summary: '修改头像',
    description: '上传新头像图片，自动替换用户头像（旧本地文件将被清理）。支持的格式：JPG/PNG/GIF/WebP，最大 5MB',
    security: [{bearerAuth: []}],
    tags: ['上传'],
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
                                    url: {
                                        type: 'string',
                                        example: '/static/avatars/uuid.jpg',
                                    },
                                },
                            },
                        },
                    },
                },
            },
        },
        400: {
            description: '参数校验失败 / 文件格式不支持',
            content: {
                'application/json': {
                    schema: errorResponse,
                },
            },
        },
        401: {
            description: '未登录',
        },
    },
});
