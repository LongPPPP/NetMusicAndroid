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
    await prisma.user.deleteMany({where: {email: testUser.email}});
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
});
