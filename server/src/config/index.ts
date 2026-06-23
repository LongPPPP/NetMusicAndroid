import dotenv from 'dotenv';

dotenv.config();

export const config = {
    port: parseInt(process.env.PORT || '3000'),
    jwt: {
        secret: process.env.JWT_SECRET || 'dev-secret-key',
        expiresIn: parseInt(process.env.JWT_EXPIRES_IN || '604800'), // 默认 7 天
    },
};
