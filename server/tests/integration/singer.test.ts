import request from 'supertest';
import app from '../../src/app';
import prisma from '../../src/config/database';

afterAll(async () => {
    await prisma.$disconnect();
});

describe('GET /singers', () => {
    it('should return paginated singer list', async () => {
        const res = await request(app).get('/api/v1/singers').expect(200);
        expect(res.body.code).toBe(200);
        expect(Array.isArray(res.body.data.list)).toBe(true);
        expect(res.body.data.total).toBeGreaterThan(0);
        expect(res.body.data.list[0]).toHaveProperty('singer_id');
        expect(res.body.data.list[0]).toHaveProperty('singer_name');
        expect(res.body.data.list[0]).toHaveProperty('avatar_url');
    });
});

describe('GET /singers/:singerId', () => {
    it('should return singer detail with hot songs', async () => {
        const res = await request(app).get('/api/v1/singers/6').expect(200);
        expect(res.body.code).toBe(200);
        expect(res.body.data.singer_id).toBe(6);
        expect(res.body.data.singer_name).toBeDefined();
        expect(res.body.data.avatar_url).toBeDefined();
        expect(Array.isArray(res.body.data.hot_songs)).toBe(true);
        expect(res.body.data.hot_songs[0]).toHaveProperty('song_id');
        expect(res.body.data.hot_songs[0]).toHaveProperty('song_name');
    });

    it('should return 404 for non-existent singer', async () => {
        const res = await request(app).get('/api/v1/singers/99999').expect(404);
        expect(res.body.code).toBe(404);
    });
});
