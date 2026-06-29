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

export const PlaylistErrorMessage = {
    NOT_FOUND: '歌单不存在',
    NAME_EXISTS: '歌单名称已存在',
    CREATE_FAILED: '创建歌单失败',
    FAVORITE_PROTECTED: '收藏歌单不可删除或改名',
} as const;

export const SongErrorMessage = {
    NOT_FOUND: '歌曲不存在或已下架',
    UNAUTHORIZED: '无权限播放此歌曲',
    NOT_OWNER: '只能操作自己的歌曲',
    NO_SINGER_PROFILE: '请先完善歌手资料',
    UPLOAD_FAILED: '歌曲上传失败',
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
