import app from './app';
import {config} from './config';

app.listen(config.port, () => {
    console.log(`🎵 NetMusic Server 启动成功`);
    console.log(`  Address:  http://localhost:${config.port}`);
    console.log(`  API:      http://localhost:${config.port}/api/v1`);
    console.log(`  API Docs: http://localhost:${config.port}/api-docs`);
});
