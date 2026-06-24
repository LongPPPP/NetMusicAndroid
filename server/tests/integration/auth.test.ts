import request from 'supertest';
import app from '../../src/app';
import prisma from '../../src/config/database';

let accessToken = '';
let refreshToken = '';
const testUser = {
    username: 'testuser',
    email: `test_${Date.now()}@example.com`,
    password: 'Test123456',
    confirmPassword: 'Test123456',
};

afterAll(async () => {
    // 清理测试用户
    await prisma.user.deleteMany({where: {email: testUser.email}});
    await prisma.$disconnect();
});

describe('Auth API', () => {
    // ===== POST /api/v1/auth/register =====
    describe('POST /auth/register', () => {
        it('should register a new user', async () => {
            const res = await request(app)
                .post('/api/v1/auth/register')
                .send(testUser)
                .expect(201);

            expect(res.body.code).toBe(201);
            expect(res.body.data.accessToken).toBeDefined();
            expect(res.body.data.refreshToken).toBeDefined();
            expect(res.body.data.user).toBeDefined();
            expect(res.body.data.user.email).toBe(testUser.email);
            expect(res.body.data.user.role).toBe('USER');
        });

        it('should reject duplicate email', async () => {
            const res = await request(app)
                .post('/api/v1/auth/register')
                .send(testUser)
                .expect(409);

            expect(res.body.code).toBe(409);
        });

        it('should reject invalid email format', async () => {
            const res = await request(app)
                .post('/api/v1/auth/register')
                .send({
                    ...testUser,
                    email: 'not-an-email',
                    username: 'baduser',
                })
                .expect(400);

            expect(res.body.code).toBe(400);
        });
    });

    // ===== POST /api/v1/auth/login =====
    describe('POST /auth/login', () => {
        it('should login with email and password', async () => {
            const res = await request(app)
                .post('/api/v1/auth/login')
                .send({email: testUser.email, password: testUser.password})
                .expect(200);

            expect(res.body.code).toBe(200);
            expect(res.body.data.accessToken).toBeDefined();
            expect(res.body.data.refreshToken).toBeDefined();
            expect(res.body.data.user.email).toBe(testUser.email);

            accessToken = res.body.data.accessToken;
            refreshToken = res.body.data.refreshToken;
        });

        it('should reject wrong password', async () => {
            const res = await request(app)
                .post('/api/v1/auth/login')
                .send({email: testUser.email, password: 'WrongPass1'})
                .expect(401);

            expect(res.body.code).toBe(401);
        });

        it('should reject non-existent email', async () => {
            const res = await request(app)
                .post('/api/v1/auth/login')
                .send({email: 'nobody@example.com', password: 'Test123456'})
                .expect(401);

            expect(res.body.code).toBe(401);
        });
    });

    // ===== POST /api/v1/auth/refresh =====
    describe('POST /auth/refresh', () => {
        it('should refresh access token', async () => {
            const res = await request(app)
                .post('/api/v1/auth/refresh')
                .send({refreshToken})
                .expect(200);

            expect(res.body.code).toBe(200);
            expect(res.body.data.accessToken).toBeDefined();
            expect(res.body.data.expiresIn).toBeDefined();
        });

        it('should reject invalid refresh token', async () => {
            const res = await request(app)
                .post('/api/v1/auth/refresh')
                .send({refreshToken: 'invalid-token'})
                .expect(401);

            expect(res.body.code).toBe(401);
        });

        it('should reject missing refresh token', async () => {
            const res = await request(app)
                .post('/api/v1/auth/refresh')
                .send({})
                .expect(400);

            expect(res.body.code).toBe(400);
        });
    });
});
