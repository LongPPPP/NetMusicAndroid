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

    // 重置 SQLite 自增序列，确保 ID 从 1 开始
    await prisma.$executeRawUnsafe(`DELETE FROM sqlite_sequence`);
    console.log('  ✅ 已重置自增序列');

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
    const bobsUser = createdUsers.find(u => u.username === 'bob')!;
    const adminsUser = createdUsers.find(u => u.username === 'admin')!;

    const singersData = [
        {name: 'Edvard Grieg',    description: '挪威浪漫主义作曲家'},
        {name: 'Chris Garneau',   description: '美国独立民谣唱作人'},
        {name: 'SC',              description: '独立电子音乐人'},
        {name: '独立音乐人',       description: '独立摇滚 / 另类流行'},
        {name: '告五人',           description: '台湾独立摇滚乐团'},
        {name: 'G.E.M. 邓紫棋',    avatarUrl: '/static/avatars/邓紫棋.jpg',   description: '华语流行 / R&B 唱作人'},
        {name: '林俊杰',           description: '华语流行 / R&B 唱作天王'},
        {name: '宋冬野',           description: '中国民谣唱作人'},
        {name: '周杰伦',           avatarUrl: '/static/avatars/周杰伦.jpg',   description: '华语流行 / 中国风代表人物'},
        {name: '米津玄师',         avatarUrl: '/static/avatars/米津玄师.jpg', userId: bobsUser.id,    description: '日本创作歌手 / 音乐制作人'},
        {name: 'Admin 官方',       userId: adminsUser.id,  description: '官方推荐账号'},
    ];

    const createdSingers: Array<{id: number; name: string}> = [];
    for (const s of singersData) {
        const singer = await prisma.singer.create({data: s});
        createdSingers.push({id: singer.id, name: singer.name});
        console.log(`  ✅ ${singer.name}${s.userId ? ` (link → user#${s.userId})` : ''}`);
    }

    // 歌手快速索引
    const S = (name: string) => createdSingers.find(s => s.name === name)!;

    // ===== 3. 歌曲 =====
    console.log('\n━━━ 3. 创建歌曲 ━━━');
    const songsData = [
        {name: "Anitra's Dance",     singerId: S('Edvard Grieg').id,   singerName: 'Edvard Grieg',   playUrl: "/static/songs/Anitra's Dance.mp3",                                                  duration: 199},
        {name: 'Not Angry',          singerId: S('Chris Garneau').id,  singerName: 'Chris Garneau',  playUrl: '/static/songs/Not Angry.mp3',          coverUrl: '/static/covers/Not Angry.jpg',          duration: 199},
        {name: 'SC',                 singerId: S('SC').id,             singerName: 'SC',             playUrl: '/static/songs/SC.mp3',                 coverUrl: '/static/covers/SC.jpg',                 duration: 201},
        {name: 'Them Kitty Bones',   singerId: S('独立音乐人').id,      singerName: '独立音乐人',      playUrl: '/static/songs/Them Kitty Bones.mp3',   coverUrl: '/static/covers/Them Kitty Bones.jpg',   duration: 227},
        {name: '唯一',                singerId: S('告五人').id,          singerName: '告五人',          playUrl: '/static/songs/唯一.mp3',                coverUrl: '/static/covers/唯一.jpg',                duration: 253},
        {name: '泡沫',                singerId: S('G.E.M. 邓紫棋').id,   singerName: 'G.E.M. 邓紫棋',   playUrl: '/static/songs/泡沫.mp3',                coverUrl: '/static/covers/泡沫.jpg',                duration: 227},
        {name: '那些你很冒险的梦',      singerId: S('林俊杰').id,         singerName: '林俊杰',         playUrl: '/static/songs/那些你很冒险的梦.mp3',      coverUrl: '/static/covers/那些你很冒险的梦.jpg',      duration: 244},
        {name: '郭源潮',              singerId: S('宋冬野').id,         singerName: '宋冬野',         playUrl: '/static/songs/郭源潮.mp3',              coverUrl: '/static/covers/郭源潮.jpg',              duration: 445},
        {name: '雨下一整晚',           singerId: S('周杰伦').id,         singerName: '周杰伦',         playUrl: '/static/songs/雨下一整晚.mp3',           coverUrl: '/static/covers/雨下一整晚.jpg',           duration: 256},
        {name: 'Lemon',              singerId: S('米津玄师').id,       singerName: '米津玄师',       playUrl: '/static/songs/Lemon.mp3',                                                duration: 256},
        {name: '打上花火',            singerId: S('米津玄师').id,       singerName: '米津玄师',       playUrl: '/static/songs/打上花火.mp3',                                              duration: 259},
    ];

    const createdSongs: Array<{id: number; name: string}> = [];
    for (const s of songsData) {
        const song = await prisma.song.create({data: s});
        createdSongs.push({id: song.id, name: song.name});
        console.log(`  ✅ ${song.name} — ${s.singerName}`);
    }

    // 歌曲快速索引
    const G = (name: string) => createdSongs.find(s => s.name === name)!;

    // ===== 4. 歌单 =====
    console.log('\n━━━ 4. 创建歌单 ━━━');
    const playlistsData = [
        // alice 的歌单
        {userId: createdUsers[0].id, name: '我最喜欢的歌'},
        {userId: createdUsers[0].id, name: '华语经典'},
        // bob 的歌单
        {userId: createdUsers[1].id, name: '摇滚精选'},
        {userId: createdUsers[1].id, name: '深夜聆听'},
        // charlie 的歌单
        {userId: createdUsers[2].id, name: '民谣小调'},
    ];

    const createdPlaylists: Array<{id: number; name: string; userId: number}> = [];
    for (const p of playlistsData) {
        const playlist = await prisma.playlist.create({data: p});
        createdPlaylists.push({id: playlist.id, name: playlist.name, userId: p.userId});
        const owner = createdUsers.find(x => x.id === p.userId)!.username;
        console.log(`  ✅ ${playlist.name} — ${owner}`);
    }

    // ===== 4b. 收藏歌单（每个用户自动创建） =====
    console.log('\n━━━ 4b. 创建收藏歌单 ━━━');
    const favoritePlaylists: Array<{id: number; userId: number}> = [];
    for (const u of createdUsers) {
        const fav = await prisma.playlist.create({
            data: {userId: u.id, name: '我的收藏', isFavorite: true},
        });
        favoritePlaylists.push({id: fav.id, userId: u.id});
        console.log(`  ✅ 我的收藏 — ${u.username}`);
    }

    const favOf = (username: string) =>
        favoritePlaylists.find(f => f.userId === createdUsers.find(u => u.username === username)!.id)!;
    const playlistOf = (name: string) =>
        createdPlaylists.find(p => p.name === name)!;

    // ===== 5. 歌单-歌曲关联 =====
    console.log('\n━━━ 5. 创建歌单-歌曲关联 ━━━');

    // alice: 我最喜欢的歌 — 6 首歌
    // alice: 华语经典 — 5 首华语歌
    // bob: 摇滚精选 — 3 首
    // bob: 深夜聆听 — 4 首
    // charlie: 民谣小调 — 3 首
    await prisma.playlistSong.createMany({
        data: [
            // --- alice: 我最喜欢的歌 ---
            {playlistId: playlistOf('我最喜欢的歌').id, songId: G("Anitra's Dance").id},
            {playlistId: playlistOf('我最喜欢的歌').id, songId: G('Not Angry').id},
            {playlistId: playlistOf('我最喜欢的歌').id, songId: G('唯一').id},
            {playlistId: playlistOf('我最喜欢的歌').id, songId: G('泡沫').id},
            {playlistId: playlistOf('我最喜欢的歌').id, songId: G('雨下一整晚').id},
            {playlistId: playlistOf('我最喜欢的歌').id, songId: G('郭源潮').id},

            // --- alice: 华语经典 ---
            {playlistId: playlistOf('华语经典').id, songId: G('唯一').id},
            {playlistId: playlistOf('华语经典').id, songId: G('泡沫').id},
            {playlistId: playlistOf('华语经典').id, songId: G('那些你很冒险的梦').id},
            {playlistId: playlistOf('华语经典').id, songId: G('雨下一整晚').id},
            {playlistId: playlistOf('华语经典').id, songId: G('郭源潮').id},

            // --- bob: 摇滚精选 ---
            {playlistId: playlistOf('摇滚精选').id, songId: G('Not Angry').id},
            {playlistId: playlistOf('摇滚精选').id, songId: G('SC').id},
            {playlistId: playlistOf('摇滚精选').id, songId: G('Them Kitty Bones').id},

            // --- bob: 深夜聆听 ---
            {playlistId: playlistOf('深夜聆听').id, songId: G('SC').id},
            {playlistId: playlistOf('深夜聆听').id, songId: G('郭源潮').id},
            {playlistId: playlistOf('深夜聆听').id, songId: G('雨下一整晚').id},
            {playlistId: playlistOf('深夜聆听').id, songId: G("Anitra's Dance").id},
            {playlistId: playlistOf('深夜聆听').id, songId: G('Lemon').id},
            {playlistId: playlistOf('深夜聆听').id, songId: G('打上花火').id},

            // --- charlie: 民谣小调 ---
            {playlistId: playlistOf('民谣小调').id, songId: G('郭源潮').id},
            {playlistId: playlistOf('民谣小调').id, songId: G('Them Kitty Bones').id},
            {playlistId: playlistOf('民谣小调').id, songId: G('Not Angry').id},

            // --- 收藏: alice (9 首，展示账号) ---
            {playlistId: favOf('alice').id, songId: G("Anitra's Dance").id},
            {playlistId: favOf('alice').id, songId: G('Not Angry').id},
            {playlistId: favOf('alice').id, songId: G('唯一').id},
            {playlistId: favOf('alice').id, songId: G('泡沫').id},
            {playlistId: favOf('alice').id, songId: G('那些你很冒险的梦').id},
            {playlistId: favOf('alice').id, songId: G('雨下一整晚').id},
            {playlistId: favOf('alice').id, songId: G('郭源潮').id},
            {playlistId: favOf('alice').id, songId: G('Lemon').id},
            {playlistId: favOf('alice').id, songId: G('打上花火').id},

            // --- 收藏: bob (7 首，展示账号) ---
            {playlistId: favOf('bob').id, songId: G('Not Angry').id},
            {playlistId: favOf('bob').id, songId: G('SC').id},
            {playlistId: favOf('bob').id, songId: G('Them Kitty Bones').id},
            {playlistId: favOf('bob').id, songId: G('郭源潮').id},
            {playlistId: favOf('bob').id, songId: G('唯一').id},
            {playlistId: favOf('bob').id, songId: G('Lemon').id},
            {playlistId: favOf('bob').id, songId: G('打上花火').id},

            // --- 收藏: charlie (3 首) ---
            {playlistId: favOf('charlie').id, songId: G('郭源潮').id},
            {playlistId: favOf('charlie').id, songId: G('Them Kitty Bones').id},
            {playlistId: favOf('charlie').id, songId: G("Anitra's Dance").id},

            // --- 收藏: admin (2 首) ---
            {playlistId: favOf('admin').id, songId: G('泡沫').id},
            {playlistId: favOf('admin').id, songId: G('那些你很冒险的梦').id},
        ],
    });
    console.log('  ✅ 已创建关联（含收藏）');

    // ===== 6. 评论 =====
    console.log('\n━━━ 6. 创建评论 ━━━');
    const U = (username: string) => createdUsers.find(u => u.username === username)!;

    const commentsData = [
        // --- Anitra's Dance ---
        {songId: G("Anitra's Dance").id, userId: U('alice').id, username: U('alice').username, content: '古典乐真的太优雅了，百听不厌！'},
        {songId: G("Anitra's Dance").id, userId: U('charlie').id, username: U('charlie').username, content: 'Grieg 的皮尔金特组曲，经典中的经典。'},

        // --- Not Angry ---
        {songId: G('Not Angry').id, userId: U('alice').id, username: U('alice').username, content: '好喜欢这首歌的旋律，轻快又治愈 🎶'},
        {songId: G('Not Angry').id, userId: U('bob').id, username: U('bob').username, content: 'Chris Garneau 的嗓音太独特了，慵懒又迷人。'},
        {songId: G('Not Angry').id, userId: U('charlie').id, username: U('charlie').username, content: '适合下雨天一个人听。'},

        // --- SC ---
        {songId: G('SC').id, userId: U('bob').id, username: U('bob').username, content: '电子音色搭配得很有层次感，值得循环！'},
        {songId: G('SC').id, userId: U('alice').id, username: U('alice').username, content: '前奏一出来就爱上了 💿'},

        // --- Them Kitty Bones ---
        {songId: G('Them Kitty Bones').id, userId: U('bob').id, username: U('bob').username, content: '这首歌的 bass line 写得太棒了！'},
        {songId: G('Them Kitty Bones').id, userId: U('charlie').id, username: U('charlie').username, content: '有态度，很喜欢这种独立音乐的风格。'},

        // --- 唯一 ---
        {songId: G('唯一').id, userId: U('alice').id, username: U('alice').username, content: '告五人的歌每次听都有不同的感受，这就是音乐的魅力吧。'},
        {songId: G('唯一').id, userId: U('alice').id, username: U('alice').username, content: '歌词写得太戳心了，"我是你的唯一" 这句话好浪漫 💕'},
        {songId: G('唯一').id, userId: U('bob').id, username: U('bob').username, content: '编曲很用心，吉他 riff 抓耳。'},

        // --- 泡沫 ---
        {songId: G('泡沫').id, userId: U('alice').id, username: U('alice').username, content: '邓紫棋的高音太震撼了！这首歌什么时候听都不过时。'},
        {songId: G('泡沫').id, userId: U('admin').id, username: U('admin').username, content: '华语乐坛的现象级歌曲，G.E.M. 的创作能力毋庸置疑。'},
        {songId: G('泡沫').id, userId: U('charlie').id, username: U('charlie').username, content: '美丽的泡沫，虽然一刹花火～'},

        // --- 那些你很冒险的梦 ---
        {songId: G('那些你很冒险的梦').id, userId: U('alice').id, username: U('alice').username, content: '林俊杰的情歌永远不会让人失望 🎤'},
        {songId: G('那些你很冒险的梦').id, userId: U('admin').id, username: U('admin').username, content: 'JJ 的嗓音加上钢琴，简直是教科书级别的情歌演绎。'},
        {songId: G('那些你很冒险的梦').id, userId: U('bob').id, username: U('bob').username, content: '副歌部分编曲层层递进，情绪饱满！'},

        // --- 郭源潮 ---
        {songId: G('郭源潮').id, userId: U('alice').id, username: U('alice').username, content: '七分多钟的民谣史诗，听完像看了一部电影。'},
        {songId: G('郭源潮').id, userId: U('bob').id, username: U('bob').username, content: '歌词太有意境了，宋冬野的叙事能力真的强。'},
        {songId: G('郭源潮').id, userId: U('charlie').id, username: U('charlie').username, content: '好喜欢这首歌，每次听都有新的感悟。'},
        {songId: G('郭源潮').id, userId: U('alice').id, username: U('alice').username, content: '这才是真正的民谣，不矫情不做作，直击人心。'},

        // --- 雨下一整晚 ---
        {songId: G('雨下一整晚').id, userId: U('alice').id, username: U('alice').username, content: '杰伦的中国风就是 yyds！中间那段二胡绝了 🎻'},
        {songId: G('雨下一整晚').id, userId: U('bob').id, username: U('bob').username, content: '编曲融合了中西元素，周杰伦的创意真是超前。'},
        {songId: G('雨下一整晚').id, userId: U('charlie').id, username: U('charlie').username, content: '这首歌的氛围感太强了，真的像在雨夜里漫步。'},
        {songId: G('雨下一整晚').id, userId: U('admin').id, username: U('admin').username, content: '被低估的好歌，不输青花瓷。'},

        // --- Lemon ---
        {songId: G('Lemon').id, userId: U('alice').id, username: U('alice').username, content: '米津玄师的 Lemon 真的是神曲！每次听都会想起《非自然死亡》🍋'},
        {songId: G('Lemon').id, userId: U('bob').id, username: U('bob').username, content: '旋律和歌词都无可挑剔，这就是 J-POP 的巅峰之作。'},
        {songId: G('Lemon').id, userId: U('alice').id, username: U('alice').username, content: '苦涩的柠檬香气，至今难以忘怀～每次听到都会跟着哼。'},

        // --- 打上花火 ---
        {songId: G('打上花火').id, userId: U('bob').id, username: U('bob').username, content: 'DAOKO 和米津玄师的合作简直天作之合！夏日烟花的画面感扑面而来 🎆'},
        {songId: G('打上花火').id, userId: U('alice').id, username: U('alice').username, content: '夏天、浴衣、花火大会，这首歌承载了太多美好的想象～'},
        {songId: G('打上花火').id, userId: U('charlie').id, username: U('charlie').username, content: '前奏的钢琴一起，整个夏天都亮了。'},
    ];

    for (const c of commentsData) {
        const comment = await prisma.comment.create({data: c});
        console.log(`  ✅ ${comment.content.slice(0, 25)}... — ${comment.username}`);
    }

    // ===== 完成 =====
    const counts = {
        users:         await prisma.user.count(),
        singers:       await prisma.singer.count(),
        songs:         await prisma.song.count(),
        playlists:     await prisma.playlist.count(),
        playlistSongs: await prisma.playlistSong.count(),
        comments:      await prisma.comment.count(),
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
