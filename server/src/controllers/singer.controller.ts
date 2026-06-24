import * as singerService from '../services/singer.service';
import {asyncHandler} from '../utils/asyncHandler';
import {success} from '../utils/response';

// 获取歌手详情
export const getSingerDetail = asyncHandler(async (req, res) => {
    const singerId = parseInt(String(req.params.id), 10);
    const result = await singerService.getSingerDetail(singerId);
    return success(res, result);
});
