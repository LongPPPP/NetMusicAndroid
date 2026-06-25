import request from 'supertest';
import app from '../../src/app';
import prisma from '../../src/config/database';

afterAll(async () => {
    await prisma.$disconnect();
});

describe('GET /search/songs', () => {
    it('should search songs by name', async () => {
        const res = await request(app).get('/api/v1/search/songs?keyword=Never').expect(200);
        expect(res.body.code).toBe(200);
        expect(res.body.data.list.length).toBeGreaterThan(0);
        expect(res.body.data.list[0]).toHaveProperty('song_name');
        expect(res.body.data.list[0]).toHaveProperty('singer_name');
    });

    it('should search songs by singer name', async () => {
        const res = await request(app).get('/api/v1/search/songs?keyword=Astley').expect(200);
        expect(res.body.code).toBe(200);
        expect(res.body.data.list.length).toBeGreaterThan(0);
    });

    it('should return empty for no match', async () => {
        const res = await request(app).get('/api/v1/search/songs?keyword=ZZZZNOEXIST').expect(200);
        expect(res.body.data.list).toEqual([]);
        expect(res.body.data.total).toBe(0);
    });

    it('should support pagination', async () => {
        const res = await request(app).get('/api/v1/search/songs?keyword=Never&page=1&page_size=5').expect(200);
        expect(res.body.data.page).toBe(1);
        expect(res.body.data.page_size).toBe(5);
    });
});

describe('GET /search/singers', () => {
    it('should search singers by name', async () => {
        const res = await request(app).get('/api/v1/search/singers?keyword=Grieg').expect(200);
        expect(res.body.code).toBe(200);
        expect(res.body.data.list.length).toBeGreaterThan(0);
        expect(res.body.data.list[0]).toHaveProperty('singer_name');
        expect(res.body.data.list[0]).toHaveProperty('avatar_url');
    });

    it('should return empty for no match', async () => {
        const res = await request(app).get('/api/v1/search/singers?keyword=ZZZZNOEXIST').expect(200);
        expect(res.body.data.list).toEqual([]);
        expect(res.body.data.total).toBe(0);
    });
});

describe('GET /search/playlists', () => {
    it('should search playlists by name', async () => {
        const res = await request(app).get('/api/v1/search/playlists?keyword=喜欢').expect(200);
        expect(res.body.code).toBe(200);
        expect(res.body.data.list.length).toBeGreaterThan(0);
        expect(res.body.data.list[0]).toHaveProperty('playlist_name');
    });

    it('should return empty for no match', async () => {
        const res = await request(app).get('/api/v1/search/playlists?keyword=ZZZZNOEXIST').expect(200);
        expect(res.body.data.list).toEqual([]);
        expect(res.body.data.total).toBe(0);
    });
});
