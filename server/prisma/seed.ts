import {PrismaLibSql} from '@prisma/adapter-libsql';
import bcrypt from 'bcryptjs';
import {PrismaClient, Gender, Role} from '../src/generated/prisma/client';

const adapter = new PrismaLibSql({
    url: process.env.DATABASE_URL || 'file:./dev.db',
});

const prisma = new PrismaClient({adapter});

// 预置用户数据
const users = [
    {
        username: 'alice',
        password: 'alice123',
        email: 'alice@example.com',
        gender: Gender.FEMALE,
        role: Role.USER,
        signature: '欢迎来到音乐世界 🎵',
    },
    {
        username: 'bob',
        password: 'bob123456',
        email: 'bob@example.com',
        gender: Gender.MALE,
        role: Role.ARTIST,
        signature: '摇滚不死',
    },
    {
        username: 'charlie',
        password: 'charlie123',
        email: 'charlie@example.com',
        gender: Gender.FEMALE,
        role: Role.USER,
        signature: '民谣爱好者',
    },
    {
        username: 'admin',
        password: 'admin123',
        email: 'admin@netmusic.com',
        gender: Gender.UNKNOWN,
        role: Role.ARTIST,
        signature: '系统管理员',
    },
];

// 预置歌手数据 — 所有图片统一使用 烟火大会.jpg
const PIC_URL = '/static/烟火大会.jpg';
const singers = [
    {name: '周杰伦', description: '华语乐坛天王，代表作《七里香》《青花瓷》'},
    {name: 'Taylor Swift', description: '美国流行天后，全球超级巨星'},
    {name: '陈奕迅', description: '香港实力派歌手，K歌之王'},
    {name: '邓紫棋', description: '香港唱作天后，铁肺歌后'},
    {name: '林俊杰', description: '新加坡歌手，行走的CD'},
];

// 预置歌曲数据
const songs = [
    {name: '七里香', singerIndex: 0, duration: 299},
    {name: '青花瓷', singerIndex: 0, duration: 273},
    {name: '稻香', singerIndex: 0, duration: 265},
    {name: '告白气球', singerIndex: 0, duration: 215},
    {name: 'Love Story', singerIndex: 1, duration: 236},
    {name: 'Shake It Off', singerIndex: 1, duration: 219},
    {name: 'Blank Space', singerIndex: 1, duration: 231},
    {name: '十年', singerIndex: 2, duration: 215},
    {name: '富士山下', singerIndex: 2, duration: 260},
    {name: '浮夸', singerIndex: 2, duration: 286},
    {name: '泡沫', singerIndex: 3, duration: 258},
    {name: '光年之外', singerIndex: 3, duration: 290},
    {name: '倒数', singerIndex: 3, duration: 227},
    {name: '江南', singerIndex: 4, duration: 264},
    {name: '不为谁而作的歌', singerIndex: 4, duration: 284},
    {name: '修炼爱情', singerIndex: 4, duration: 257},
];

// 预置歌单
const playlists = [
    {userIndex: 0, name: '我的最爱'},
    {userIndex: 0, name: '华语经典'},
    {userIndex: 2, name: '跑步必备'},
];

// 歌单-歌曲关联
const playlistSongEntries = [
    {playlistIndex: 0, songIndices: [0, 1, 3, 10, 13]},
    {playlistIndex: 1, songIndices: [0, 1, 2, 7, 8, 13, 14]},
    {playlistIndex: 2, songIndices: [3, 5, 6, 11]},
];

// 预置评论
const commentEntries = [
    {songIndex: 0, userIndex: 0, content: '夏天的味道，满满的回忆！'},
    {songIndex: 0, userIndex: 1, content: '经典永不过时'},
    {songIndex: 1, userIndex: 2, content: '天青色等烟雨，而我在等你'},
    {songIndex: 4, userIndex: 0, content: 'Taylor Swift 永远的神！'},
    {songIndex: 7, userIndex: 3, content: '十年之前，我不认识你'},
    {songIndex: 10, userIndex: 1, content: '泡沫这首歌太好听了'},
];

async function main() {
    console.log('🌱 开始播种测试数据...\n');

    // 1. 清除已有数据（注意外键顺序）
    await prisma.comment.deleteMany();
    await prisma.playlistSong.deleteMany();
    await prisma.song.deleteMany();
    await prisma.singer.deleteMany();
    await prisma.playlist.deleteMany();
    await prisma.user.deleteMany();
    console.log('  已清除所有数据\n');

    // 2. 创建用户
    const createdUsers = [];
    for (const u of users) {
        const hashedPassword = await bcrypt.hash(u.password, 10);
        const user = await prisma.user.create({
            data: {
                username: u.username,
                password: hashedPassword,
                email: u.email,
                gender: u.gender,
                role: u.role,
                signature: u.signature,
            },
        });
        createdUsers.push(user);
        console.log(`  ✅ 创建用户: ${user.username} (${user.email})`);
    }

    // 3. 创建歌手
    const createdSingers = [];
    for (const s of singers) {
        const singer = await prisma.singer.create({
            data: {
                name: s.name,
                avatarUrl: PIC_URL,
                description: s.description,
            },
        });
        createdSingers.push(singer);
        console.log(`  ✅ 创建歌手: ${singer.name}`);
    }

    // 4. 创建歌曲
    const createdSongs = [];
    for (const s of songs) {
        const singer = createdSingers[s.singerIndex];
        const song = await prisma.song.create({
            data: {
                name: s.name,
                singerId: singer.id,
                singerName: singer.name,
                playUrl: PIC_URL,
                coverUrl: PIC_URL,
                duration: s.duration,
            },
        });
        createdSongs.push(song);
    }
    console.log(`  ✅ 创建 ${createdSongs.length} 首歌曲`);

    // 5. 创建歌单
    const createdPlaylists = [];
    for (const p of playlists) {
        const user = createdUsers[p.userIndex];
        const playlist = await prisma.playlist.create({
            data: {userId: user.id, name: p.name},
        });
        createdPlaylists.push(playlist);
    }
    console.log(`  ✅ 创建 ${createdPlaylists.length} 个歌单`);

    // 6. 关联歌单和歌曲
    for (const entry of playlistSongEntries) {
        const playlist = createdPlaylists[entry.playlistIndex];
        for (const si of entry.songIndices) {
            await prisma.playlistSong.create({
                data: {playlistId: playlist.id, songId: createdSongs[si].id},
            });
        }
    }
    console.log('  ✅ 歌单-歌曲关联完成');

    // 7. 创建评论
    for (const c of commentEntries) {
        const user = createdUsers[c.userIndex];
        const song = createdSongs[c.songIndex];
        await prisma.comment.create({
            data: {
                songId: song.id,
                userId: user.id,
                username: user.username,
                content: c.content,
            },
        });
    }
    console.log(`  ✅ 创建 ${commentEntries.length} 条评论`);

    console.log(`\n🎉 播种完成！`);
    console.log(`   ${createdUsers.length} 用户`);
    console.log(`   ${createdSingers.length} 歌手`);
    console.log(`   ${createdSongs.length} 歌曲`);
    console.log(`   ${createdPlaylists.length} 歌单`);
    console.log(`   ${commentEntries.length} 评论`);
}

main()
    .catch((e) => {
        console.error('❌ 播种失败:', e);
        process.exit(1);
    })
    .finally(async () => {
        await prisma.$disconnect();
    });
