import {Router} from 'express';
import * as singerController from '../controllers/singer.controller';
import {validateQuery} from '../middlewares/validate';
import {getSingersSchema} from '../validators/singer.validator';

const router = Router();

router.get('/', validateQuery(getSingersSchema), singerController.listSingers);
router.get('/:singerId', singerController.getSingerDetail);

export default router;
