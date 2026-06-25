import cors from 'cors';
import express from 'express';
import path from 'path';
import swaggerUi from 'swagger-ui-express';
import {generateOpenAPIDocument} from './docs';
import {errorMiddleware, notFoundMiddleware} from './middlewares/error.middleware';
import {loggerMiddleware} from './middlewares/logger.middleware';
import routes from './routes';

const app = express();

// ===== 全局中间件 =====
app.use(cors());                    // 跨域
app.use(express.json());            // JSON 解析
app.use(loggerMiddleware);          // 请求日志

// ===== API 文档（Swagger UI）=====
const openapiDoc = generateOpenAPIDocument();
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(openapiDoc));
app.get('/api-docs.json', (_req, res) => res.json(openapiDoc));

// ===== 静态文件托管 =====
app.use('/static', express.static('storage'));

// ===== 路由 =====
app.use('/api/v1', routes);

// ===== 错误处理 =====
app.use(notFoundMiddleware);        // 404
app.use(errorMiddleware);           // 全局错误

export default app;
