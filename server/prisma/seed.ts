import {PrismaLibSql} from '@prisma/adapter-libsql';
import bcrypt from 'bcryptjs';
import {PrismaClient, Role} from '../src/generated/prisma/client';

const adapter = new PrismaLibSql({
    url: process.env.DATABASE_URL || 'file:./prisma/dev.db',
});

const prisma = new PrismaClient({adapter});

async function main() {
    console.log('🌱 开始播种测试数据...\n');

    // ===== 清除旧数据（按外键约束逆序） =====
    console.log('━━━ 清除旧数据 ━━━');
    await prisma.playlistSong.deleteMany();
    console.log('  ✅ 已清除歌单-歌曲关联');
    await prisma.comment.deleteMany();
    console.log('  ✅ 已清除评论');
    await prisma.playlist.deleteMany();
    console.log('  ✅ 已清除歌单');
    await prisma.song.deleteMany();
    console.log('  ✅ 已清除歌曲');
    await prisma.singer.deleteMany();
    console.log('  ✅ 已清除歌手');
    const deletedUsers = await prisma.user.deleteMany();
    console.log(`  ✅ 已清除 ${deletedUsers.count} 个用户`);

    // ===== 1. 用户 =====
    console.log('\n━━━ 1. 创建用户 ━━━');
    const usersData = [
        {username: 'alice',      password: 'alice123',   email: 'alice@example.com',   role: Role.USER,   signature: '欢迎来到音乐世界 🎵'},
        {username: 'bob',        password: 'bob123456',  email: 'bob@example.com',     role: Role.ARTIST, signature: '摇滚不死'},
        {username: 'charlie',    password: 'charlie123', email: 'charlie@example.com', role: Role.USER,   signature: '民谣爱好者'},
        {username: 'admin',      password: 'admin123',   email: 'admin@netmusic.com',  role: Role.ARTIST, signature: '系统管理员'},
    ];

    const createdUsers: Array<{id: number; username: string}> = [];
    for (const u of usersData) {
        const user = await prisma.user.create({
            data: {...u, password: await bcrypt.hash(u.password, 10)},
        });
        createdUsers.push({id: user.id, username: user.username});
        console.log(`  ✅ ${user.username} (${user.email})`);
    }

    // ===== 2. 歌手 =====
    console.log('\n━━━ 2. 创建歌手 ━━━');
    const singersData = [
        {name: 'Edvard Grieg', description: '挪威浪漫主义作曲家'},
        {name: 'Rick Astley', avatarUrl: '/static/avatars/Rick Astley.webp', description: '80 年代英伦流行 / 蓝眼灵魂'},
    ];

    const createdSingers: Array<{id: number; name: string}> = [];
    for (const s of singersData) {
        const singer = await prisma.singer.create({data: s});
        createdSingers.push({id: singer.id, name: singer.name});
        console.log(`  ✅ ${singer.name}`);
    }

    // ===== 3. 歌曲 =====
    console.log('\n━━━ 3. 创建歌曲 ━━━');
    const songsData = [
        {name: 'Anitra\'s Dance',   singerId: createdSingers[0].id, singerName: createdSingers[0].name, playUrl: '/static/songs/Anitra\'s Dance.mp3', duration: 199},
        {name: 'Never Gonna Give You Up', singerId: createdSingers[1].id, singerName: createdSingers[1].name, playUrl: '/static/songs/Never Gonna Give You Up.mp3', coverUrl: '/static/covers/cover-Never Gonna Give You Up.jpg', duration: 214},
    ];

    const createdSongs: Array<{id: number; name: string}> = [];
    for (const s of songsData) {
        const song = await prisma.song.create({data: s});
        createdSongs.push({id: song.id, name: song.name});
        console.log(`  ✅ ${song.name} — ${createdSingers.find(x => x.id === s.singerId)!.name}`);
    }

    // ===== 4. 歌单 =====
    console.log('\n━━━ 4. 创建歌单 ━━━');
    const playlistsData = [
        {userId: createdUsers[0].id, name: '我最喜欢的歌'},
        {userId: createdUsers[1].id, name: '摇滚精选'},
    ];

    const createdPlaylists: Array<{id: number; name: string}> = [];
    for (const p of playlistsData) {
        const playlist = await prisma.playlist.create({data: p});
        createdPlaylists.push({id: playlist.id, name: playlist.name});
        console.log(`  ✅ ${playlist.name} — ${createdUsers.find(x => x.id === p.userId)!.username}`);
    }

    // ===== 5. 歌单-歌曲关联 =====
    console.log('\n━━━ 5. 创建歌单-歌曲关联 ━━━');
    await prisma.playlistSong.createMany({
        data: [
            {playlistId: createdPlaylists[0].id, songId: createdSongs[0].id},
            {playlistId: createdPlaylists[0].id, songId: createdSongs[1].id},
            {playlistId: createdPlaylists[1].id, songId: createdSongs[1].id},
        ],
    });
    console.log('  ✅ 已创建 3 条关联');

    // ===== 6. 评论 =====
    console.log('\n━━━ 6. 创建评论 ━━━');
    const commentsData = [
        {songId: createdSongs[0].id, userId: createdUsers[0].id, username: createdUsers[0].username, content: '这首歌太好听了！'},
        {songId: createdSongs[1].id, userId: createdUsers[1].id, username: createdUsers[1].username, content: 'Classic!'},
    ];

    for (const c of commentsData) {
        const comment = await prisma.comment.create({data: c});
        console.log(`  ✅ ${comment.content.slice(0, 20)}... — ${comment.username}`);
    }

    // ===== 完成 =====
    const counts = {
        users:    await prisma.user.count(),
        singers:  await prisma.singer.count(),
        songs:    await prisma.song.count(),
        playlists: await prisma.playlist.count(),
        playlistSongs: await prisma.playlistSong.count(),
        comments: await prisma.comment.count(),
    };
    console.log(`\n🎉 播种完成！`);
    console.log(`   用户 ${counts.users} | 歌手 ${counts.singers} | 歌曲 ${counts.songs}`);
    console.log(`   歌单 ${counts.playlists} | 歌单歌曲 ${counts.playlistSongs} | 评论 ${counts.comments}`);
}

main()
    .catch((e) => {
        console.error('❌ 播种失败:', e);
        process.exit(1);
    })
    .finally(async () => {
        await prisma.$disconnect();
    });
