import type {Config} from 'jest';

// 确保测试使用正确的数据库文件
process.env.DATABASE_URL = 'file:./dev.db';

const config: Config = {
    preset: 'ts-jest',
    testEnvironment: 'node',
    roots: ['<rootDir>/tests'],
    testMatch: ['**/*.test.ts'],
    clearMocks: true,
    transform: {
        '^.+\\.ts$': ['ts-jest', {tsconfig: 'tsconfig.test.json'}],
    },
};

export default config;
