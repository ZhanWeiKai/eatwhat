# 配置说明

## 智谱AI配置

本项目使用智谱AI提供AI美食顾问功能，需要配置API密钥。

### 获取API密钥

1. 访问 [智谱AI开放平台](https://open.bigmodel.cn/)
2. 注册并登录账号
3. 进入控制台，获取API Key

### 本地开发配置

**方法1：使用application-local.yml（推荐）**

1. 复制配置文件模板：
```bash
cp springboot-backend/src/main/resources/application-local.yml.example springboot-backend/src/main/resources/application-local.yml
```

2. 编辑 `application-local.yml`，填入你的API密钥：
```yaml
zhipuai:
  api-key: 你的API密钥
```

3. 启动应用时使用local配置：
```bash
mvn spring-boot:run -Dspring.profiles.active=local
```

**方法2：使用环境变量**

设置环境变量：
```bash
# Windows
set ZHIPUAI_API_KEY=你的API密钥

# Linux/Mac
export ZHIPUAI_API_KEY=你的API密钥

# PowerShell
$env:ZHIPUAI_API_KEY="你的API密钥"
```

### 生产环境配置

**使用Docker Compose：**

在 `docker-compose.yml` 中设置：
```yaml
services:
  app:
    environment:
      - ZHIPUAI_API_KEY=${ZHIPUAI_API_KEY}
```

创建 `.env` 文件（不提交到Git）：
```
ZHIPUAI_API_KEY=你的生产环境API密钥
```

**直接设置环境变量：**
```bash
# 在服务器上
export ZHIPUAI_API_KEY=你的生产环境API密钥
```

### 注意事项

- ⚠️ **不要**将 `application-local.yml` 提交到Git
- ⚠️ **不要**在公开仓库中暴露API密钥
- ✅ `.gitignore` 已配置忽略此文件
- ✅ 每个开发者使用自己的API密钥

### 验证配置

启动应用后，访问：
```
http://localhost:8883/api/ai/chat
```

发送测试请求：
```json
{"message": "今天中午吃什么？"}
```

如果返回AI回复，说明配置成功。
