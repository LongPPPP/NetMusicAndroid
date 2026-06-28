import request from 'supertest';
import app from '../../src/app';
import prisma from '../../src/config/database';

const ts = Date.now();
let accessToken = '';
let artistToken = '';
let artistSingerId = 0;
let commentId = 0;

const testUser = {
    username: `st${ts}`,
    email: `st${ts}@e.co`,
    password: 'Test123456',
    confirmPassword: 'Test123456',
};

beforeAll(async () => {
    await request(app).post('/api/v1/auth/register').send(testUser);
    const loginRes = await request(app)
        .post('/api/v1/auth/login')
        .send({email: testUser.email, password: testUser.password});
    accessToken = loginRes.body.data.access_token;

    // 登录 seed 中的 ARTIST 用户（bob）用于上架/下架测试
    const artistLogin = await request(app)
        .post('/api/v1/auth/login')
        .send({email: 'bob@example.com', password: 'bob123456'});
    artistToken = artistLogin.body.data.access_token;

    const singer = await prisma.singer.findUnique({
        where: {userId: artistLogin.body.data.user.id},
        select: {id: true},
    });
    artistSingerId = singer!.id;
});

afterAll(async () => {
    // 先删除关联歌单（含收藏歌单），再删用户
    const user = await prisma.user.findUnique({where: {email: testUser.email}, select: {id: true}});
    if (user) {
        await prisma.playlist.deleteMany({where: {userId: user.id}});
        await prisma.user.deleteMany({where: {email: testUser.email}});
    }
    await prisma.$disconnect();
});

describe('GET /songs', () => {
    it('should return paginated song list', async () => {
        const res = await request(app).get('/api/v1/songs').expect(200);
        expect(res.body.code).toBe(200);
        expect(Array.isArray(res.body.data.list)).toBe(true);
        expect(res.body.data.total).toBeGreaterThan(0);
        expect(res.body.data.list[0]).toHaveProperty('song_id');
        expect(res.body.data.list[0]).toHaveProperty('song_name');
        expect(res.body.data.list[0]).toHaveProperty('singer_name');
        expect(res.body.data.list[0]).toHaveProperty('cover_url');
        expect(res.body.data.list[0]).not.toHaveProperty('play_url');
    });

    it('should filter by singer_id', async () => {
        const res = await request(app).get('/api/v1/songs?singer_id=1').expect(200);
        expect(res.body.code).toBe(200);
        expect(res.body.data.total).toBe(1);
    });

    it('should support pagination', async () => {
        const res = await request(app).get('/api/v1/songs?page=1&page_size=3').expect(200);
        expect(res.body.data.list.length).toBeLessThanOrEqual(3);
        expect(res.body.data.page_size).toBe(3);
    });

    it('should reject invalid page param', async () => {
        await request(app).get('/api/v1/songs?page=-1').expect(400);
    });
});

describe('GET /songs/:songId', () => {
    it('should return song detail', async () => {
        const res = await request(app).get('/api/v1/songs/1').expect(200);
        expect(res.body.code).toBe(200);
        expect(res.body.data.song_id).toBe(1);
        expect(res.body.data.song_name).toBeDefined();
        expect(res.body.data.play_url).toBeDefined();
        expect(res.body.data.duration).toBeGreaterThan(0);
    });

    it('should return 404 for non-existent song', async () => {
        const res = await request(app).get('/api/v1/songs/99999').expect(404);
        expect(res.body.code).toBe(404);
    });
});

describe('Comment API', () => {
    describe('GET /songs/:songId/comments', () => {
        it('should return paginated comments', async () => {
            const res = await request(app).get('/api/v1/songs/1/comments').expect(200);
            expect(res.body.code).toBe(200);
            expect(Array.isArray(res.body.data.list)).toBe(true);
            expect(res.body.data.total).toBeGreaterThan(0);
            expect(res.body.data.list[0]).toHaveProperty('comment_id');
            expect(res.body.data.list[0]).toHaveProperty('content');
        });

        it('should return 404 for non-existent song', async () => {
            await request(app).get('/api/v1/songs/99999/comments').expect(404);
        });
    });

    describe('POST /songs/:songId/comments', () => {
        it('should create a comment', async () => {
            const res = await request(app)
                .post('/api/v1/songs/1/comments')
                .set('Authorization', `Bearer ${accessToken}`)
                .send({content: '测试评论'})
                .expect(201);
            expect(res.body.code).toBe(201);
            expect(res.body.data.comment_id).toBeGreaterThan(0);
            expect(res.body.data.content).toBe('测试评论');
            commentId = res.body.data.comment_id;
        });

        it('should reject empty content', async () => {
            await request(app)
                .post('/api/v1/songs/1/comments')
                .set('Authorization', `Bearer ${accessToken}`)
                .send({content: ''})
                .expect(400);
        });

        it('should reject over 500 chars', async () => {
            await request(app)
                .post('/api/v1/songs/1/comments')
                .set('Authorization', `Bearer ${accessToken}`)
                .send({content: 'a'.repeat(501)})
                .expect(400);
        });

        it('should reject without auth', async () => {
            await request(app)
                .post('/api/v1/songs/1/comments')
                .send({content: 'test'})
                .expect(401);
        });
    });

    describe('DELETE /songs/:songId/comments/:commentId', () => {
        it('should delete own comment', async () => {
            const res = await request(app)
                .delete(`/api/v1/songs/1/comments/${commentId}`)
                .set('Authorization', `Bearer ${accessToken}`)
                .expect(200);
            expect(res.body.code).toBe(200);
        });

        it('should return 404 for deleted comment', async () => {
            await request(app)
                .delete(`/api/v1/songs/1/comments/${commentId}`)
                .set('Authorization', `Bearer ${accessToken}`)
                .expect(404);
        });
    });
});

// ===== 歌曲上架/下架（ARTIST 专属） =====
describe('Song Management (ARTIST)', () => {
    let uploadedSongId = 0;
    const dummyMp3 = Buffer.from('dummy audio');

    describe('POST /songs', () => {
        it('should reject without auth', async () => {
            await request(app).post('/api/v1/songs').expect(401);
        });

        it('should reject USER role', async () => {
            await request(app)
                .post('/api/v1/songs')
                .set('Authorization', `Bearer ${accessToken}`)
                .field('name', 'test')
                .expect(403);
        });

        it('should reject empty name', async () => {
            await request(app)
                .post('/api/v1/songs')
                .set('Authorization', `Bearer ${artistToken}`)
                .field('name', '')
                .expect(400);
        });

        it('should reject missing song file', async () => {
            const res = await request(app)
                .post('/api/v1/songs')
                .set('Authorization', `Bearer ${artistToken}`)
                .field('name', '无文件歌曲')
                .expect(400);
            expect(res.body.message).toBeDefined();
        });

        it('should upload a song successfully', async () => {
            const res = await request(app)
                .post('/api/v1/songs')
                .set('Authorization', `Bearer ${artistToken}`)
                .field('name', '测试上架歌曲')
                .attach('song', dummyMp3, 'test.mp3')
                .expect(201);
            expect(res.body.code).toBe(201);
            expect(res.body.data.song_name).toBe('测试上架歌曲');
            expect(res.body.data.singer_name).toBeDefined();
            expect(res.body.data.play_url).toMatch(/^\/static\/songs\//);
            uploadedSongId = res.body.data.song_id;
        });
    });

    describe('DELETE /songs/:songId', () => {
        it('should reject without auth', async () => {
            await request(app).delete(`/api/v1/songs/${uploadedSongId}`).expect(401);
        });

        it('should reject USER role', async () => {
            await request(app)
                .delete(`/api/v1/songs/${uploadedSongId}`)
                .set('Authorization', `Bearer ${accessToken}`)
                .expect(403);
        });

        it('should reject deleting others song', async () => {
            // song#1 是 Edvard Grieg 的，不属于 bob
            await request(app)
                .delete('/api/v1/songs/1')
                .set('Authorization', `Bearer ${artistToken}`)
                .expect(403);
        });

        it('should delete own song', async () => {
            const res = await request(app)
                .delete(`/api/v1/songs/${uploadedSongId}`)
                .set('Authorization', `Bearer ${artistToken}`)
                .expect(200);
            expect(res.body.code).toBe(200);
        });

        it('should return 404 for deleted song', async () => {
            await request(app)
                .delete(`/api/v1/songs/${uploadedSongId}`)
                .set('Authorization', `Bearer ${artistToken}`)
                .expect(404);
        });
    });
});

// ===== 收藏（toggle） =====
describe('Favorite API', () => {
    describe('POST /songs/:songId/favorite', () => {
        it('should reject without auth', async () => {
            await request(app).post('/api/v1/songs/1/favorite').expect(401);
        });

        it('should toggle favorite on', async () => {
            const res = await request(app)
                .post('/api/v1/songs/1/favorite')
                .set('Authorization', `Bearer ${accessToken}`)
                .expect(200);
            expect(res.body.data.favorited).toBe(true);
        });

        it('should toggle favorite off', async () => {
            const res = await request(app)
                .post('/api/v1/songs/1/favorite')
                .set('Authorization', `Bearer ${accessToken}`)
                .expect(200);
            expect(res.body.data.favorited).toBe(false);
        });

        it('should return 404 for non-existent song', async () => {
            await request(app)
                .post('/api/v1/songs/99999/favorite')
                .set('Authorization', `Bearer ${accessToken}`)
                .expect(404);
        });
    });
});
