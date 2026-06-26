/** 用户提示消息，统一管理消除魔法字符串 */
export const AuthErrorMessage = {
    USERNAME_EXISTS: '用户名已被占用',
    EMAIL_EXISTS: '邮箱已被注册',
    USER_NOT_FOUND: '用户不存在',
    LOGIN_FAILED: '用户名或密码错误',
    REGISTER_FAILED: '注册失败',
    LOGIN_ERROR: '登录失败',
    TOKEN_INVALID: 'Token 验证失败',
    REFRESH_TOKEN_INVALID: 'Refresh Token 无效或已过期',
    FORBIDDEN: '权限不足，无法执行此操作',
} as const;

export const UserErrorMessage = {
    USERNAME_LENGTH: '用户名长度需在 1-16 个字符之间',
    AVATAR_FORMAT: '头像地址格式不正确',
    SIGNATURE_MAX: '个性签名最多 100 个字符',
    INVALID_FIELD: '不支持修改的字段',
} as const;

export const PlaylistErrorMessage = {
    NOT_FOUND: '歌单不存在',
    NAME_EXISTS: '歌单名称已存在',
    CREATE_FAILED: '创建歌单失败',
    NO_PERMISSION: '无权操作此歌单',
    SONG_NOT_FOUND: '歌曲不存在',
    SONG_ALREADY_IN_PLAYLIST: '歌曲已在歌单中',
    SONG_NOT_IN_PLAYLIST: '歌曲不在该歌单中',
} as const;

export const SongErrorMessage = {
    NOT_FOUND: '歌曲不存在或已下架',
    UNAUTHORIZED: '无权限播放此歌曲',
    ARTIST_ONLY: '仅歌手（ARTIST）可执行此操作',
    NO_SINGER_PROFILE: '未找到您的歌手档案，请联系管理员',
    CANNOT_DELETE_OTHERS_SONG: '只能删除自己的歌曲',
} as const;

export const SingerErrorMessage = {
    NOT_FOUND: '歌手不存在',
} as const;

export const CommentErrorMessage = {
    SONG_NOT_FOUND: '歌曲不存在',
    NOT_FOUND: '评论不存在',
    CONTENT_TOO_LONG: '评论内容过长',
    CREATE_FAILED: '发表评论失败',
    DELETE_FAILED: '删除评论失败',
    NOT_AUTHOR: '只能删除自己的评论',
} as const;
