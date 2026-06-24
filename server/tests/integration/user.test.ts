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
    // 注册测试用户并保存 token
    const res = await request(app)
        .post('/api/v1/auth/register')
        .send(testUser);

    accessToken = res.body.data.accessToken;
    userId = res.body.data.user.id;
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

    // ===== PATCH /users/me/username =====
    describe('PATCH /users/me/username', () => {
        const newName = 'updated_name';

        it('should update username', async () => {
            const res = await request(app)
                .patch('/api/v1/users/me/username')
                .set('Authorization', `Bearer ${accessToken}`)
                .send({username: newName})
                .expect(200);

            expect(res.body.code).toBe(200);
            expect(res.body.data.username).toBe(newName);
        });

        it('should reject empty username', async () => {
            const res = await request(app)
                .patch('/api/v1/users/me/username')
                .set('Authorization', `Bearer ${accessToken}`)
                .send({username: ''})
                .expect(400);
        });
    });

    // ===== PATCH /users/me/signature =====
    describe('PATCH /users/me/signature', () => {
        const newSig = '新的个性签名';

        it('should update signature', async () => {
            const res = await request(app)
                .patch('/api/v1/users/me/signature')
                .set('Authorization', `Bearer ${accessToken}`)
                .send({signature: newSig})
                .expect(200);

            expect(res.body.code).toBe(200);
            expect(res.body.data.signature).toBe(newSig);
        });

        it('should reject signature over 100 chars', async () => {
            const res = await request(app)
                .patch('/api/v1/users/me/signature')
                .set('Authorization', `Bearer ${accessToken}`)
                .send({signature: 'a'.repeat(101)})
                .expect(400);
        });
    });

    // ===== PATCH /users/me/email =====
    describe('PATCH /users/me/email', () => {
        const newEmail = `new_email_${Date.now()}@example.com`;

        it('should update email', async () => {
            const res = await request(app)
                .patch('/api/v1/users/me/email')
                .set('Authorization', `Bearer ${accessToken}`)
                .send({email: newEmail})
                .expect(200);

            expect(res.body.code).toBe(200);
            expect(res.body.data.email).toBe(newEmail);
        });

        it('should reject invalid email format', async () => {
            const res = await request(app)
                .patch('/api/v1/users/me/email')
                .set('Authorization', `Bearer ${accessToken}`)
                .send({email: 'not-an-email'})
                .expect(400);
        });
    });

    // ===== PATCH /users/me/avatar =====
    describe('PATCH /users/me/avatar', () => {
        const avatarUrl = 'https://example.com/avatar.jpg';

        it('should update avatar', async () => {
            const res = await request(app)
                .patch('/api/v1/users/me/avatar')
                .set('Authorization', `Bearer ${accessToken}`)
                .send({avatar: avatarUrl})
                .expect(200);

            expect(res.body.code).toBe(200);
            expect(res.body.data.avatar).toBe(avatarUrl);
        });

        it('should reject invalid url', async () => {
            const res = await request(app)
                .patch('/api/v1/users/me/avatar')
                .set('Authorization', `Bearer ${accessToken}`)
                .send({avatar: 'not-a-url'})
                .expect(400);
        });
    });

    // ===== Auth guard for all PATCH =====
    describe('Auth guard', () => {
        it('should reject all PATCH without token', async () => {
            await request(app)
                .patch('/api/v1/users/me/username')
                .send({username: 'test'})
                .expect(401);

            await request(app)
                .patch('/api/v1/users/me/avatar')
                .send({avatar: 'http://example.com/a.jpg'})
                .expect(401);

            await request(app)
                .patch('/api/v1/users/me/signature')
                .send({signature: 'test'})
                .expect(401);

            await request(app)
                .patch('/api/v1/users/me/email')
                .send({email: 'test@example.com'})
                .expect(401);
        });
    });
});
