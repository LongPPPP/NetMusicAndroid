import {Router} from 'express';
import * as searchController from '../controllers/search.controller';
import {validateQuery} from '../middlewares/validate';
import {searchSongsSchema, searchSingersSchema, searchPlaylistsSchema} from '../validators/search.validator';

const router = Router();

router.get('/songs', validateQuery(searchSongsSchema), searchController.searchSongs);
router.get('/singers', validateQuery(searchSingersSchema), searchController.searchSingers);
router.get('/playlists', validateQuery(searchPlaylistsSchema), searchController.searchPlaylists);

export default router;
