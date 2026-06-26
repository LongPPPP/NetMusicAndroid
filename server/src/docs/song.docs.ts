import {z} from 'zod';
import {registry} from '../config/openapi';
import {createCommentSchema, getCommentsSchema, getSongsSchema} from '../validators/song.validator';

const songIdParam = z.object({songId: z.coerce.number().int().positive().describe('歌曲 ID')});

const errorResponse = {
    type: 'object' as const,
    properties: {
        code: {type: 'integer' as const, example: 400},
        message: {type: 'string' as const, example: '参数校验失败'},
        data: {type: 'null' as const},
    },
};

// ===== GET /songs =====
registry.registerPath({
    method: 'get',
    path: '/songs',
    summary: '分页获取歌曲列表（支持按歌手获取）',
    tags: ['歌曲'],
    request: {query: getSongsSchema},
    responses: {
        200: {
            description: '返回歌曲列表（分页）',
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
                                                song_id: {type: 'integer'},
                                                song_name: {type: 'string'},
                                                singer_name: {type: 'string'},
                                                play_url: {type: 'string', nullable: true},
                                                duration: {type: 'integer', nullable: true},
                                            },
                                        },
                                    },
                                    total: {type: 'integer'},
                                    page: {type: 'integer'},
                                    page_size: {type: 'integer'},
                                },
                                example: {
                                    list: [
                                        {song_id: 1, song_name: '稻香', singer_name: '周杰伦', play_url: '/static/songs/daoxiang.mp3', duration: 244},
                                        {song_id: 2, song_name: '晴天', singer_name: '周杰伦', play_url: '/static/songs/qingtian.mp3', duration: 267},
                                    ],
                                    total: 2,
                                    page: 1,
                                    page_size: 20,
                                },
                            },
                        },
                    },
                },
            },
        },
        400: {
            description: '参数校验失败（页码或每页数量不合法）',
            content: {
                'application/json': {
                    schema: errorResponse,
                },
            },
        },
    },
});

// ===== GET /songs/:id =====
registry.registerPath({
    method: 'get',
    path: '/songs/{songId}',
    summary: '获取歌曲详情',
    tags: ['歌曲'],
    request: {params: songIdParam},
    responses: {
        200: {
            description: '返回歌曲详情',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 200},
                            data: {
                                type: 'object',
                                properties: {
                                    song_id: {type: 'integer'},
                                    song_name: {type: 'string'},
                                    singer_name: {type: 'string'},
                                    play_url: {type: 'string', nullable: true},
                                    cover_url: {type: 'string', nullable: true},
                                    duration: {type: 'integer', nullable: true},
                                },
                                example: {
                                    song_id: 1,
                                    song_name: '稻香',
                                    singer_name: '周杰伦',
                                    play_url: '/static/songs/daoxiang.mp3',
                                    cover_url: '/static/covers/daoxiang.jpg',
                                    duration: 244,
                                },
                            },
                        },
                    },
                },
            },
        },
        400: {
            description: '路径参数不合法（歌曲 ID 必须为正整数）',
            content: {
                'application/json': {
                    schema: errorResponse,
                },
            },
        },
        404: {description: '歌曲不存在'},
    },
});

// ===== GET /songs/:id/comments =====
registry.registerPath({
    method: 'get',
    path: '/songs/{songId}/comments',
    summary: '分页获取歌曲评论列表',
    tags: ['歌曲'],
    request: {params: songIdParam, query: getCommentsSchema},
    responses: {
        200: {
            description: '返回评论列表（分页）',
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
                                                user_id: {type: 'integer'},
                                                username: {type: 'string', nullable: true},
                                                content: {type: 'string'},
                                            },
                                        },
                                    },
                                    total: {type: 'integer'},
                                    page: {type: 'integer'},
                                    page_size: {type: 'integer'},
                                },
                                example: {
                                    list: [
                                        {comment_id: 1, user_id: 1, username: '小明', content: '这首歌太好听了！'},
                                        {comment_id: 2, user_id: 2, username: '小红', content: '经典永流传'},
                                    ],
                                    total: 2,
                                    page: 1,
                                    page_size: 20,
                                },
                            },
                        },
                    },
                },
            },
        },
        400: {
            description: '参数校验失败（页码或每页数量不合法）',
            content: {
                'application/json': {
                    schema: errorResponse,
                },
            },
        },
        404: {description: '歌曲不存在'},
    },
});

// ===== POST /songs/:id/comments =====
registry.registerPath({
    method: 'post',
    path: '/songs/{songId}/comments',
    summary: '发表评论',
    security: [{bearerAuth: []}],
    tags: ['歌曲'],
    request: {
        params: songIdParam,
        body: {
            content: {
                'application/json': {schema: createCommentSchema},
            },
        },
    },
    responses: {
        201: {
            description: '评论成功',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 201},
                            message: {type: 'string', example: '评论成功'},
                            data: {
                                type: 'object',
                                properties: {
                                    comment_id: {type: 'integer'},
                                    user_id: {type: 'integer'},
                                    username: {type: 'string', nullable: true},
                                    content: {type: 'string'},
                                },
                                example: {
                                    comment_id: 1,
                                    user_id: 1,
                                    username: '小明',
                                    content: '这首歌太好听了！',
                                },
                            },
                        },
                    },
                },
            },
        },
        400: {
            description: '参数校验失败（评论内容为空或过长）',
            content: {
                'application/json': {
                    schema: errorResponse,
                },
            },
        },
        401: {description: '未登录'},
        404: {description: '歌曲不存在'},
    },
});

// ===== DELETE /songs/:songId/comments/:commentId =====
const commentParams = z.object({
    songId: z.coerce.number().int().positive().describe('歌曲 ID'),
    commentId: z.coerce.number().int().positive().describe('评论 ID'),
});

registry.registerPath({
    method: 'delete',
    path: '/songs/{songId}/comments/{commentId}',
    summary: '删除评论',
    description: '仅评论作者可删除自己的评论',
    security: [{bearerAuth: []}],
    tags: ['歌曲'],
    request: {params: commentParams},
    responses: {
        200: {
            description: '删除成功',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 200},
                            message: {type: 'string', example: '删除成功'},
                            data: {type: 'null'},
                        },
                    },
                },
            },
        },
        400: {
            description: '路径参数不合法',
            content: {
                'application/json': {
                    schema: errorResponse,
                },
            },
        },
        401: {description: '未登录'},
        403: {description: '无权删除他人评论'},
        404: {description: '评论不存在'},
    },
});