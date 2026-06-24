import {registry} from '../config/openapi';

// ===== POST /upload/avatar =====
registry.registerPath({
    method: 'post',
    path: '/upload/avatar',
    summary: '上传头像',
    description: '上传头像图片，返回可访问的 URL。支持的格式：JPG/PNG/GIF/WebP，最大 5MB',
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
                                        example: '/uploads/avatars/uuid.jpg',
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
        },
        401: {
            description: '未登录',
        },
    },
});
