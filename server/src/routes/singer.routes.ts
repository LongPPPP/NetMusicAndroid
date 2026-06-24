import {Router} from 'express';
import * as singerController from '../controllers/singer.controller';

const router = Router();

router.get('/:id', singerController.getSingerDetail);

export default router;
