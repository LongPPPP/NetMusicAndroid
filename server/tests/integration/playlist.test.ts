import request from 'supertest';
import app from '../../src/app';
import prisma from '../../src/config/database';

const ts = Date.now();
let accessToken = '';
let playlistId = 0;

const testUser = {
    username: `pl${ts}`,
    email: `pl${ts}@e.co`,
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

describe('GET /playlists/:playlistId', () => {
    it('should return playlist detail with songs', async () => {
        const res = await request(app).get('/api/v1/playlists/1').expect(200);
        expect(res.body.code).toBe(200);
        expect(res.body.data.playlist_id).toBe(1);
        expect(res.body.data.playlist_name).toBeDefined();
        expect(Array.isArray(res.body.data.songs)).toBe(true);
        expect(res.body.data.songs.length).toBeGreaterThan(0);
        expect(res.body.data.songs[0]).toHaveProperty('song_id');
        expect(res.body.data.songs[0]).toHaveProperty('song_name');
        expect(res.body.data.songs[0]).toHaveProperty('singer_name');
    });

    it('should return 404 for non-existent playlist', async () => {
        await request(app).get('/api/v1/playlists/99999').expect(404);
    });
});

describe('POST /playlists', () => {
    it('should create a playlist', async () => {
        const res = await request(app)
            .post('/api/v1/playlists')
            .set('Authorization', `Bearer ${accessToken}`)
            .send({name: '测试歌单'})
            .expect(201);
        expect(res.body.code).toBe(201);
        expect(res.body.data.playlist_id).toBeGreaterThan(0);
        playlistId = res.body.data.playlist_id;
    });

    it('should reject empty name', async () => {
        await request(app)
            .post('/api/v1/playlists')
            .set('Authorization', `Bearer ${accessToken}`)
            .send({name: ''})
            .expect(400);
    });

    it('should reject name over 30 chars', async () => {
        await request(app)
            .post('/api/v1/playlists')
            .set('Authorization', `Bearer ${accessToken}`)
            .send({name: 'a'.repeat(31)})
            .expect(400);
    });

    it('should reject without auth', async () => {
        await request(app)
            .post('/api/v1/playlists')
            .send({name: 'test'})
            .expect(401);
    });

    it('should reject duplicate name', async () => {
        await request(app)
            .post('/api/v1/playlists')
            .set('Authorization', `Bearer ${accessToken}`)
            .send({name: '测试歌单'})
            .expect(409);
    });
});

describe('POST /playlists/:playlistId/songs', () => {
    it('should add a song', async () => {
        await request(app)
            .post(`/api/v1/playlists/${playlistId}/songs`)
            .set('Authorization', `Bearer ${accessToken}`)
            .send({song_id: 1})
            .expect(201);
    });

    it('should reject duplicate song', async () => {
        await request(app)
            .post(`/api/v1/playlists/${playlistId}/songs`)
            .set('Authorization', `Bearer ${accessToken}`)
            .send({song_id: 1})
            .expect(409);
    });

    it('should reject non-existent song', async () => {
        await request(app)
            .post(`/api/v1/playlists/${playlistId}/songs`)
            .set('Authorization', `Bearer ${accessToken}`)
            .send({song_id: 99999})
            .expect(404);
    });

    it('should reject without auth', async () => {
        await request(app)
            .post(`/api/v1/playlists/${playlistId}/songs`)
            .send({song_id: 2})
            .expect(401);
    });
});

describe('DELETE /playlists/:playlistId/songs/:songId', () => {
    it('should remove song from playlist', async () => {
        await request(app)
            .delete(`/api/v1/playlists/${playlistId}/songs/1`)
            .set('Authorization', `Bearer ${accessToken}`)
            .expect(200);
    });

    it('should return 404 for already removed song', async () => {
        await request(app)
            .delete(`/api/v1/playlists/${playlistId}/songs/1`)
            .set('Authorization', `Bearer ${accessToken}`)
            .expect(404);
    });
});

describe('PATCH /playlists/:playlistId', () => {
    it('should rename playlist', async () => {
        const res = await request(app)
            .patch(`/api/v1/playlists/${playlistId}`)
            .set('Authorization', `Bearer ${accessToken}`)
            .send({name: '已重命名'})
            .expect(200);
        expect(res.body.data.playlist_name).toBe('已重命名');
    });

    it('should reject without auth', async () => {
        await request(app)
            .patch(`/api/v1/playlists/${playlistId}`)
            .send({name: 'test'})
            .expect(401);
    });
});

describe('DELETE /playlists/:playlistId', () => {
    it('should delete playlist', async () => {
        await request(app)
            .delete(`/api/v1/playlists/${playlistId}`)
            .set('Authorization', `Bearer ${accessToken}`)
            .expect(200);
    });

    it('should return 404 for deleted playlist', async () => {
        await request(app)
            .delete(`/api/v1/playlists/${playlistId}`)
            .set('Authorization', `Bearer ${accessToken}`)
            .expect(404);
    });

    it('should reject other user\'s playlist', async () => {
        await request(app)
            .delete('/api/v1/playlists/2')
            .set('Authorization', `Bearer ${accessToken}`)
            .expect(403);
    });
});
