import { OpenApiGeneratorV3, OpenAPIRegistry } from '@asteasolutions/zod-to-openapi';

// 全局 OpenAPI 注册表
export const registry = new OpenAPIRegistry();

// 注册 JWT Bearer 安全方案
registry.registerComponent('securitySchemes', 'bearerAuth', {
  type: 'http',
  scheme: 'bearer',
  bearerFormat: 'JWT',
});

// 生成 OpenAPI 规范文档
export function generateOpenAPIDocument() {
  const generator = new OpenApiGeneratorV3(registry.definitions);

  return generator.generateDocument({
    openapi: '3.0.3',
    info: {
      title: 'NetMusic API',
      version: '1.0.0',
      description: 'NetMusic 音乐应用后端接口文档',
    },
    servers: [{ url: '/api/v1', description: 'API v1' }],
  });
}
