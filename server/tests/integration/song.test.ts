import request from 'supertest';
import app from '../../src/app';
import prisma from '../../src/config/database';

const ts = Date.now();
let accessToken = '';
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
});

afterAll(async () => {
    await prisma.user.deleteMany({where: {email: testUser.email}});
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
