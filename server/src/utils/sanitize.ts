import xss from 'xss';

const xssOptions = {
    whiteList: {},        // 不允许任何 HTML 标签
    stripIgnoreTag: true, // 移除无法识别的标签
    stripIgnoreTagBody: ['script', 'style'], // 移除 script/style 标签及其内容
};

/**
 * 过滤字符串中的 XSS / 特殊字符
 */
export function sanitize(input: string): string {
    return xss(input.trim(), xssOptions);
}

/**
 * 批量过滤对象中的所有字符串字段
 */
export function sanitizeObject<T extends Record<string, any>>(obj: T): T {
    const result: Record<string, any> = {};
    for (const [key, value] of Object.entries(obj)) {
        result[key] = typeof value === 'string' ? sanitize(value) : value;
    }
    return result as T;
}