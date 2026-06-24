import * as searchService from '../services/search.service';
import {asyncHandler} from '../utils/asyncHandler';
import {success} from '../utils/response';

// 搜索歌曲
export const search = asyncHandler(async (req, res) => {
    const keyword = String(req.query.keyword || '');
    const result = await searchService.searchSongs(keyword);
    return success(res, result);
});
