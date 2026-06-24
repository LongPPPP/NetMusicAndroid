import dotenv from 'dotenv';
import path from 'path';
import {PrismaLibSql} from '@prisma/adapter-libsql';
import {PrismaClient} from '../generated/prisma/client';

// 立即加载 .env，确保 DATABASE_URL 在任何数据库连接前生效
dotenv.config();

const dbUrl = process.env.DATABASE_URL || `file:${path.resolve(__dirname, '../../dev.db')}`;

const adapter = new PrismaLibSql({
    url: dbUrl,
});

const prisma = new PrismaClient({adapter});

export default prisma;
