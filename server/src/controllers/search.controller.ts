import * as searchService from '../services/search.service';
import {asyncHandler} from '../utils/asyncHandler';
import {success} from '../utils/response';

// 搜索歌曲
export const searchSongs = asyncHandler(async (req, res) => {
    const {keyword, page, page_size} = req.query as any;
    const result = await searchService.searchSongs(keyword || '', page || 1, page_size || 20);
    return success(res, result);
});

// 搜索歌手
export const searchSingers = asyncHandler(async (req, res) => {
    const {keyword, page, page_size} = req.query as any;
    const result = await searchService.searchSingers(keyword || '', page || 1, page_size || 20);
    return success(res, result);
});

// 搜索歌单
export const searchPlaylists = asyncHandler(async (req, res) => {
    const {keyword, page, page_size} = req.query as any;
    const result = await searchService.searchPlaylists(keyword || '', page || 1, page_size || 20);
    return success(res, result);
});
