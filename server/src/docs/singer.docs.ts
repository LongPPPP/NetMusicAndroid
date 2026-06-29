import {z} from 'zod';
import {registry} from '../config/openapi';
import {getSingersSchema} from '../validators/singer.validator';

const singerIdParam = z.object({singerId: z.coerce.number().int().positive().describe('歌手 ID')});

const errorResponse = {
    type: 'object' as const,
    properties: {
        code: {type: 'integer' as const, example: 400},
        message: {type: 'string' as const, example: '参数校验失败'},
        data: {type: 'null' as const},
    },
};

// ===== GET /singers =====
registry.registerPath({
    method: 'get',
    path: '/singers',
    summary: '分页获取歌手列表',
    tags: ['歌手'],
    request: {query: getSingersSchema},
    responses: {
        200: {
            description: '返回歌手列表（分页）',
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
                                                singer_id: {type: 'integer'},
                                                singer_name: {type: 'string'},
                                                avatar_url: {type: 'string', nullable: true},
                                            },
                                        },
                                    },
                                    total: {type: 'integer'},
                                    page: {type: 'integer'},
                                    page_size: {type: 'integer'},
                                },
                                example: {
                                    list: [
                                        {singer_id: 1, singer_name: '周杰伦', avatar_url: null},
                                        {singer_id: 2, singer_name: 'Taylor Swift', avatar_url: '/static/avatars/taylor.jpg'},
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

// ===== GET /singers/:id =====
registry.registerPath({
    method: 'get',
    path: '/singers/{singerId}',
    summary: '获取歌手详情（含热门歌曲）',
    tags: ['歌手'],
    request: {params: singerIdParam},
    responses: {
        200: {
            description: '返回歌手详情及热门歌曲',
            content: {
                'application/json': {
                    schema: {
                        type: 'object',
                        properties: {
                            code: {type: 'integer', example: 200},
                            data: {
                                type: 'object',
                                properties: {
                                    singer_id: {type: 'integer'},
                                    singer_name: {type: 'string'},
                                    avatar_url: {type: 'string', nullable: true},
                                    description: {type: 'string', nullable: true},
                                    hot_songs: {
                                        type: 'array',
                                        description: '热门歌曲列表（最多返回 10 首）',
                                        items: {
                                            type: 'object',
                                            properties: {
                                                song_id: {type: 'integer'},
                                                song_name: {type: 'string'},
                                                singer_id: {type: 'integer', nullable: true},
                                                cover_url: {type: 'string', nullable: true},
                                                duration: {type: 'integer', nullable: true},
                                            },
                                        },
                                    },
                                },
                                example: {
                                    singer_id: 1,
                                    singer_name: '周杰伦',
                                    avatar_url: null,
                                    description: '台湾著名歌手、音乐制作人',
                                    hot_songs: [
                                        {song_id: 1, song_name: '稻香', singer_id: 1, cover_url: '/static/covers/daoxiang.jpg', duration: 244},
                                        {song_id: 2, song_name: '晴天', singer_id: 1, cover_url: null, duration: 267},
                                        {song_id: 3, song_name: '七里香', singer_id: 1, cover_url: '/static/covers/qilixiang.jpg', duration: 299},
                                    ],
                                },
                            },
                        },
                    },
                },
            },
        },
        400: {
            description: '路径参数不合法（歌手 ID 必须为正整数）',
            content: {
                'application/json': {
                    schema: errorResponse,
                },
            },
        },
        404: {description: '歌手不存在'},
    },
});