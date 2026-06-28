import request from 'supertest';
import app from '../../src/app';
import prisma from '../../src/config/database';

let accessToken = '';
let userId = 0;
const testUser = {
    username: 'testuser_profile',
    email: `test_profile_${Date.now()}@example.com`,
    password: 'Test123456',
    confirmPassword: 'Test123456',
};

beforeAll(async () => {
    // 先注册测试用户
    await request(app)
        .post('/api/v1/auth/register')
        .send(testUser);

    // 再登录获取 token 和 userId
    const loginRes = await request(app)
        .post('/api/v1/auth/login')
        .send({email: testUser.email, password: testUser.password});

    accessToken = loginRes.body.data.access_token;
    userId = loginRes.body.data.user.id;
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

describe('User API', () => {
    // ===== GET /users/:id =====
    describe('GET /users/:id', () => {
        it('should return user public profile', async () => {
            const res = await request(app)
                .get(`/api/v1/users/${userId}`)
                .expect(200);

            expect(res.body.code).toBe(200);
            expect(res.body.data.username).toBe(testUser.username);
            expect(res.body.data.email).toBe(testUser.email);
            expect(res.body.data.role).toBe('USER');
            expect(res.body.data).toHaveProperty('comment_count');
            expect(res.body.data).toHaveProperty('favorite_count');
            expect(typeof res.body.data.comment_count).toBe('number');
            expect(typeof res.body.data.favorite_count).toBe('number');
        });

        it('should return 404 for non-existent user', async () => {
            const res = await request(app)
                .get('/api/v1/users/99999')
                .expect(404);

            expect(res.body.code).toBe(404);
        });

        it('should return 400 for invalid user id', async () => {
            const res = await request(app)
                .get('/api/v1/users/abc')
                .expect(400);

            expect(res.body.code).toBe(400);
        });
    });

    // ===== GET /users/me =====
    describe('GET /users/me', () => {
        it('should return current user profile', async () => {
            const res = await request(app)
                .get('/api/v1/users/me')
                .set('Authorization', `Bearer ${accessToken}`)
                .expect(200);

            expect(res.body.code).toBe(200);
            expect(res.body.data.email).toBe(testUser.email);
        });

        it('should reject without auth', async () => {
            const res = await request(app)
                .get('/api/v1/users/me')
                .expect(401);

            expect(res.body.code).toBe(401);
        });
    });

    // ===== PATCH /users/me (unified) =====
    describe('PATCH /users/me', () => {
        it('should update username', async () => {
            const res = await request(app)
                .patch('/api/v1/users/me')
                .set('Authorization', `Bearer ${accessToken}`)
                .send({field: 'username', value: 'updated_name'})
                .expect(200);

            expect(res.body.code).toBe(200);
            expect(res.body.data.username).toBe('updated_name');
        });

        it('should update signature', async () => {
            const res = await request(app)
                .patch('/api/v1/users/me')
                .set('Authorization', `Bearer ${accessToken}`)
                .send({field: 'signature', value: '新的个性签名'})
                .expect(200);

            expect(res.body.code).toBe(200);
            expect(res.body.data.signature).toBe('新的个性签名');
        });

        it('should update email', async () => {
            const newEmail = `new_email_${Date.now()}@example.com`;
            const res = await request(app)
                .patch('/api/v1/users/me')
                .set('Authorization', `Bearer ${accessToken}`)
                .send({field: 'email', value: newEmail})
                .expect(200);

            expect(res.body.code).toBe(200);
            expect(res.body.data.email).toBe(newEmail);
        });

        it('should update avatar', async () => {
            const avatarUrl = 'https://example.com/avatar.jpg';
            const res = await request(app)
                .patch('/api/v1/users/me')
                .set('Authorization', `Bearer ${accessToken}`)
                .send({field: 'avatar', value: avatarUrl})
                .expect(200);

            expect(res.body.code).toBe(200);
            expect(res.body.data.avatar).toBe(avatarUrl);
        });

        it('should reject invalid field', async () => {
            const res = await request(app)
                .patch('/api/v1/users/me')
                .set('Authorization', `Bearer ${accessToken}`)
                .send({field: 'invalid', value: 'test'})
                .expect(400);
        });

        it('should reject empty value', async () => {
            const res = await request(app)
                .patch('/api/v1/users/me')
                .set('Authorization', `Bearer ${accessToken}`)
                .send({field: 'username', value: ''})
                .expect(400);
        });
    });

    // ===== Auth guard =====
    describe('Auth guard', () => {
        it('should reject PATCH /users/me without token', async () => {
            await request(app)
                .patch('/api/v1/users/me')
                .send({field: 'username', value: 'test'})
                .expect(401);
        });
    });

    // ===== Favorites =====
    describe('GET /users/me/favorites', () => {
        it('should return my favorite playlist', async () => {
            const res = await request(app)
                .get('/api/v1/users/me/favorites')
                .set('Authorization', `Bearer ${accessToken}`)
                .expect(200);

            expect(res.body.code).toBe(200);
            expect(res.body.data).toHaveProperty('playlist_id');
            expect(res.body.data).toHaveProperty('songs');
            expect(res.body.data.playlist_name).toBe('我的收藏');
            expect(Array.isArray(res.body.data.songs)).toBe(true);
            if (res.body.data.songs.length > 0) {
                expect(res.body.data.songs[0]).toHaveProperty('cover_url');
                expect(res.body.data.songs[0]).not.toHaveProperty('play_url');
            }
        });

        it('should reject without auth', async () => {
            await request(app)
                .get('/api/v1/users/me/favorites')
                .expect(401);
        });
    });

    describe('GET /users/:userId/favorites', () => {
        it('should return public user favorite playlist', async () => {
            // 先收藏一首歌，确保有数据
            await request(app)
                .post('/api/v1/songs/1/favorite')
                .set('Authorization', `Bearer ${accessToken}`);

            const res = await request(app)
                .get(`/api/v1/users/${userId}/favorites`)
                .expect(200);

            expect(res.body.code).toBe(200);
            expect(res.body.data).toHaveProperty('playlist_id');
            expect(res.body.data).toHaveProperty('songs');
            expect(res.body.data.playlist_name).toBe('我的收藏');
            expect(res.body.data.total).toBeGreaterThanOrEqual(1);
            if (res.body.data.songs.length > 0) {
                expect(res.body.data.songs[0]).toHaveProperty('cover_url');
                expect(res.body.data.songs[0]).not.toHaveProperty('play_url');
            }

            // 清理：取消收藏
            await request(app)
                .post('/api/v1/songs/1/favorite')
                .set('Authorization', `Bearer ${accessToken}`);
        });

        it('should return 400 for invalid user id', async () => {
            await request(app)
                .get('/api/v1/users/abc/favorites')
                .expect(400);
        });
    });
});
