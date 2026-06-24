import {z} from 'zod';
import {registry} from '../config/openapi';
import {addPlaylistSongSchema, createPlaylistSchema} from '../validators/playlist.validator';

const playlistIdParam = z.object({playlistId: z.coerce.number().int().positive().describe('歌单 ID')});
const playlistSongParams = z.object({
    playlistId: z.coerce.number().int().positive().describe('歌单 ID'),
    songId: z.coerce.number().int().positive().describe('歌曲 ID'),
});

const errorResponse = {
    type: 'object' as const,
    properties: {
        code: {type: 'integer' as const, example: 400},
        message: {type: 'string' as const, example: '参数校验失败'},
        data: {type: 'null' as const},
    },
};

// ===== GET /playlists =====
registry.registerPath({
    method: 'get',
    path: '/playlists',
    summary: '获取用户歌单列表',
    tags: ['歌单'],
    request: {
        query: z.object({
            user_id: z.string().describe('用户 ID'),
        }),
    },
    responses: {
        200: {
            description: '返回歌单列表',
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
                                                created_at: {type: 'string', format: 'date-time', description: '创建时间'},
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
        400: {
            description: '参数校验失败（user_id 不能为空）',
            content: {
                'application/json': {
                    schema: errorResponse,
                },
            },
        },
    },
});

// ===== GET /playlists/me =====
registry.registerPath({
    method: 'get',
    path: '/playlists/me',
    summary: '获取我的歌单列表',
    description: '通过 token 自动获取当前登录用户的歌单列表，无需传 user_id',
    security: [{bearerAuth: []}],
    tags: ['歌单'],
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
                                                created_at: {type: 'string', format: 'date-time', description: '创建时间'},
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

// ===== GET /playlists/:id =====
registry.registerPath({
    method: 'get',
    path: '/playlists/{playlistId}',
    summary: '获取歌单详情（含歌曲列表）',
    tags: ['歌单'],
    request: {params: playlistIdParam},
    responses: {
        200: {
            description: '返回歌单及歌曲列表',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 200},
                            data: {
                                type: 'object',
                                properties: {
                                    playlist_id: {type: 'integer'},
                                    playlist_name: {type: 'string'},
                                    songs: {
                                        type: 'array',
                                        items: {
                                            type: 'object',
                                            properties: {
                                                song_id: {type: 'integer'},
                                                song_name: {type: 'string'},
                                                singer_name: {type: 'string'},
                                            },
                                        },
                                    },
                                },
                                example: {
                                    playlist_id: 1,
                                    playlist_name: '我的最爱',
                                    songs: [
                                        {song_id: 1, song_name: '稻香', singer_name: '周杰伦'},
                                        {song_id: 2, song_name: '晴天', singer_name: '周杰伦'},
                                    ],
                                },
                            },
                        },
                    },
                },
            },
        },
        400: {
            description: '路径参数不合法（歌单 ID 必须为正整数）',
            content: {
                'application/json': {
                    schema: errorResponse,
                },
            },
        },
        404: {description: '歌单不存在'},
    },
});

// ===== POST /playlists =====
registry.registerPath({
    method: 'post',
    path: '/playlists',
    summary: '创建歌单',
    security: [{bearerAuth: []}],
    tags: ['歌单'],
    request: {
        body: {
            content: {
                'application/json': {schema: createPlaylistSchema},
            },
        },
    },
    responses: {
        201: {
            description: '创建成功',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 201},
                            message: {type: 'string', example: '创建歌单成功'},
                            data: {
                                type: 'object',
                                properties: {playlist_id: {type: 'integer'}},
                                example: {playlist_id: 1},
                            },
                        },
                    },
                },
            },
        },
        400: {
            description: '参数校验失败（歌单名称为空或过长）',
            content: {
                'application/json': {
                    schema: errorResponse,
                },
            },
        },
        401: {description: '未登录'},
        409: {description: '歌单名称已存在'},
    },
});

// ===== POST /playlists/:id/songs =====
registry.registerPath({
    method: 'post',
    path: '/playlists/{playlistId}/songs',
    summary: '歌单添加歌曲',
    security: [{bearerAuth: []}],
    tags: ['歌单'],
    request: {
        params: playlistIdParam,
        body: {
            content: {
                'application/json': {schema: addPlaylistSongSchema},
            },
        },
    },
    responses: {
        201: {
            description: '添加成功',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 201},
                            message: {type: 'string', example: '添加成功'},
                            data: {type: 'null'},
                        },
                    },
                },
            },
        },
        400: {
            description: '参数校验失败（歌曲 ID 不合法）',
            content: {
                'application/json': {
                    schema: errorResponse,
                },
            },
        },
        401: {description: '未登录'},
        403: {description: '无权操作此歌单'},
        404: {description: '歌单或歌曲不存在'},
        409: {description: '歌曲已在歌单中'},
    },
});

// ===== DELETE /playlists/:id/songs/:songId =====
registry.registerPath({
    method: 'delete',
    path: '/playlists/{playlistId}/songs/{songId}',
    summary: '歌单移除歌曲',
    security: [{bearerAuth: []}],
    tags: ['歌单'],
    request: {params: playlistSongParams},
    responses: {
        200: {
            description: '移除成功',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 200},
                            message: {type: 'string', example: '移除成功'},
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
        403: {description: '无权操作此歌单'},
        404: {description: '歌单或歌曲不存在'},
    },
});

// ===== PATCH /playlists/:id =====
registry.registerPath({
    method: 'patch',
    path: '/playlists/{playlistId}',
    summary: '重命名歌单',
    security: [{bearerAuth: []}],
    tags: ['歌单'],
    request: {
        params: playlistIdParam,
        body: {
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            name: {type: 'string', example: '新歌单名称', description: '歌单新名称（1-30 字符）'},
                        },
                        required: ['name'],
                    },
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
                            data: {
                                type: 'object',
                                properties: {
                                    playlist_id: {type: 'integer'},
                                    playlist_name: {type: 'string'},
                                    song_count: {type: 'integer'},
                                    created_at: {type: 'string', format: 'date-time'},
                                },
                                example: {
                                    playlist_id: 1,
                                    playlist_name: '新歌单名称',
                                    song_count: 5,
                                    created_at: '2024-01-15T08:30:00.000Z',
                                },
                            },
                        },
                    },
                },
            },
        },
        400: {
            description: '参数校验失败（名称为空或过长）',
            content: {
                'application/json': {
                    schema: errorResponse,
                },
            },
        },
        401: {description: '未登录'},
        403: {description: '无权操作此歌单'},
        404: {description: '歌单不存在'},
        409: {description: '歌单名称已存在'},
    },
});

// ===== DELETE /playlists/:id =====
registry.registerPath({
    method: 'delete',
    path: '/playlists/{playlistId}',
    summary: '删除歌单',
    security: [{bearerAuth: []}],
    tags: ['歌单'],
    request: {params: playlistIdParam},
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
        403: {description: '无权操作此歌单'},
        404: {description: '歌单不存在'},
    },
});