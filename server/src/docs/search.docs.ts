import {registry} from '../config/openapi';
import {searchSongsSchema, searchSingersSchema, searchPlaylistsSchema} from '../validators/search.validator';

const errorResponse = {
    type: 'object' as const,
    properties: {
        code: {type: 'integer' as const, example: 400},
        message: {type: 'string' as const, example: '参数校验失败'},
        data: {type: 'null' as const},
    },
};

// ===== GET /search/songs =====
registry.registerPath({
    method: 'get',
    path: '/search/songs',
    summary: '搜索歌曲',
    description: '按歌曲名或歌手名搜索歌曲，支持分页',
    tags: ['搜索'],
    request: {query: searchSongsSchema},
    responses: {
        200: {
            description: '返回歌曲搜索结果（分页）',
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
                                            },
                                        },
                                    },
                                    total: {type: 'integer', description: '搜索结果总数'},
                                    page: {type: 'integer', description: '当前页码'},
                                    page_size: {type: 'integer', description: '每页数量'},
                                },
                                example: {
                                    list: [
                                        {song_id: 1, song_name: '稻香', singer_name: '周杰伦'},
                                        {song_id: 2, song_name: '晴天', singer_name: '周杰伦'},
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
            description: '参数校验失败（关键字为空、过长等）',
            content: {
                'application/json': {
                    schema: errorResponse,
                },
            },
        },
    },
});

// ===== GET /search/singers =====
registry.registerPath({
    method: 'get',
    path: '/search/singers',
    summary: '搜索歌手',
    description: '按歌手名搜索歌手，支持分页',
    tags: ['搜索'],
    request: {query: searchSingersSchema},
    responses: {
        200: {
            description: '返回歌手搜索结果（分页）',
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
                                    total: {type: 'integer', description: '搜索结果总数'},
                                    page: {type: 'integer', description: '当前页码'},
                                    page_size: {type: 'integer', description: '每页数量'},
                                },
                                example: {
                                    list: [
                                        {singer_id: 1, singer_name: '周杰伦', avatar_url: null},
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
        400: {
            description: '参数校验失败（关键字为空、过长等）',
            content: {
                'application/json': {
                    schema: errorResponse,
                },
            },
        },
    },
});

// ===== GET /search/playlists =====
registry.registerPath({
    method: 'get',
    path: '/search/playlists',
    summary: '搜索歌单',
    description: '按歌单名搜索歌单，支持分页',
    tags: ['搜索'],
    request: {query: searchPlaylistsSchema},
    responses: {
        200: {
            description: '返回歌单搜索结果（分页）',
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
                                            },
                                        },
                                    },
                                    total: {type: 'integer', description: '搜索结果总数'},
                                    page: {type: 'integer', description: '当前页码'},
                                    page_size: {type: 'integer', description: '每页数量'},
                                },
                                example: {
                                    list: [
                                        {playlist_id: 1, playlist_name: '周杰伦精选'},
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
        400: {
            description: '参数校验失败（关键字为空、过长等）',
            content: {
                'application/json': {
                    schema: errorResponse,
                },
            },
        },
    },
});
