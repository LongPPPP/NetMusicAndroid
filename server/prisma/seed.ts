import { PrismaClient } from '../src/generated/prisma/client';
import { PrismaLibSql } from '@prisma/adapter-libsql';
import bcrypt from 'bcryptjs';

const adapter = new PrismaLibSql({
  url: process.env.DATABASE_URL || 'file:./prisma/dev.db',
});

const prisma = new PrismaClient({ adapter });

// 预置用户数据
const users = [
  {
    username: 'alice',
    password: 'alice123',
    nickname: '爱丽丝',
    email: 'alice@example.com',
    gender: 1,
    signature: '欢迎来到音乐世界 🎵',
  },
  {
    username: 'bob',
    password: 'bob123456',
    nickname: '鲍勃',
    email: 'bob@example.com',
    gender: 1,
    signature: '摇滚不死',
  },
  {
    username: 'charlie',
    password: 'charlie123',
    nickname: '查理',
    email: 'charlie@example.com',
    gender: 2,
    signature: '民谣爱好者',
  },
  {
    username: 'admin',
    password: 'admin123',
    nickname: '管理员',
    email: 'admin@netmusic.com',
    gender: 0,
    signature: '系统管理员',
  },
];

async function main() {
  console.log('🌱 开始播种测试数据...\n');

  // 先清除已有数据（方便重复运行）
  const deleted = await prisma.user.deleteMany();
  console.log(`  已清除 ${deleted.count} 条用户数据`);

  // 逐条创建
  for (const u of users) {
    const hashedPassword = await bcrypt.hash(u.password, 10);
    const user = await prisma.user.create({
      data: {
        username: u.username,
        password: hashedPassword,
        nickname: u.nickname,
        email: u.email,
        gender: u.gender,
        signature: u.signature,
      },
    });
    console.log(`  ✅ 创建用户: ${user.username} (${user.email})`);
  }

  console.log(`\n🎉 播种完成！共创建 ${users.length} 个用户`);
}

main()
  .catch((e) => {
    console.error('❌ 播种失败:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });