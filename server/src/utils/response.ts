import {Response} from 'express';

// 成功响应
export function success(res: Response, data: any, message: string = 'success', code: number = 200) {
    return res.status(code).json({code, message, data});
}

// 错误响应
export function fail(res: Response, message: string, code: number = 400, httpStatus?: number) {
    return res.status(httpStatus || code).json({code, message, data: null});
}
