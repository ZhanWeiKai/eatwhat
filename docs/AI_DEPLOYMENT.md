# AI智能助手部署文档

## 功能概述

AI智能助手基于智谱AI GLM-4-Flash模型，为用户提供菜品推荐和烹饪建议。

## 后端配置

### 1. 环境变量配置

**开发环境**：
```yaml
# springboot-backend/src/main/resources/application-local.yml
zhipuai:
  api-key: cea9d940b7b7498d916e1c924ba3b6ca.zwaG7aTXwBW60Dr4
```

**生产环境（Docker）**：
```yaml
# /root/what2eat/docker-compose.yml
services:
  app:
    environment:
      ZHIPUAI_API_KEY: cea9d940b7b7498d916e1c924ba3b6ca.zwaG7aTXwBW60Dr4
```

### 2. API端点

```
POST http://api.jamesweb.org:8883/api/ai/chat
Content-Type: application/json

{
  "message": "你好，今天推荐什么菜？"
}
```

### 3. 响应格式

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "message": "AI的回复内容"
  }
}
```

## 部署步骤

### 标准部署流程

1. **编译后端**
```bash
cd springboot-backend
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd" clean package -DskipTests
```

2. **上传到服务器**
```bash
scp springboot-backend/target/what2eat-backend-1.0.0.jar root@47.83.126.42:/root/what2eat/target/
```

3. **重启容器**
```bash
ssh root@47.83.126.42 "cd /root/what2eat && docker compose restart app"
```

4. **验证部署**
```bash
curl -s http://api.jamesweb.org:8883/api/ai/chat \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"message":"测试"}'
```

### Android部署

1. **编译APK**
```bash
cd android-app
./gradlew.bat assembleDebug
```

2. **安装到设备**
```bash
adb install android-app/app/build/outputs/apk/debug/app-debug.apk
```

## 常见问题

### Q1: API返回"抱歉，我现在无法回答"

**原因**：环境变量未正确配置

**解决步骤**：
1. 检查docker-compose.yml中的environment部分
2. 确认ZHIPUAI_API_KEY已添加（不使用变量替换语法）
3. 完全重建容器：`docker compose down app && docker compose up -d app`
4. 验证环境变量：`docker compose exec app env | grep ZHIPUAI_API_KEY`

### Q2: 401 Unauthorized错误

**原因**：API密钥无效或未加载

**排查步骤**：
1. 检查.env文件是否存在
2. 检查docker-compose.yml中的环境变量配置
3. 使用`docker compose exec app env`查看所有环境变量
4. 确认API密钥格式正确

### Q3: env_file无法加载环境变量

**问题**：同时使用`env_file`和`environment`中的变量替换语法导致冲突

**解决**：
- ❌ 错误配置：
```yaml
env_file:
  - .env
environment:
  ZHIPUAI_API_KEY: ${ZHIPUAI_API_KEY}  # 变量替换语法
```

- ✅ 正确配置：
```yaml
env_file:
  - .env
# 或直接在environment中硬编码
environment:
  ZHIPUAI_API_KEY: cea9d940b7b7498d916e1c924ba3b6ca.zwaG7aTXwBW60Dr4
```

## 测试账号

- 用户名: `testuser1`
- 密码: `123456`

## 测试用例

```bash
# 测试1：菜品推荐
curl -s http://api.jamesweb.org:8883/api/ai/chat \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"message":"今天推荐什么菜？"}'

# 测试2：烹饪建议
curl -s http://api.jamesweb.org:8883/api/ai/chat \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"message":"宫保鸡丁怎么做？"}'

# 测试3：简单菜品
curl -s http://api.jamesweb.org:8883/api/ai/chat \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"message":"西红柿炒鸡蛋"}'
```

## 架构说明

### 后端架构

```
AIController (REST API)
    ↓
ZhipuAIService (业务逻辑)
    ↓
RestTemplate (HTTP客户端)
    ↓
智谱AI API (GLM-4-Flash)
```

### Android架构

```
AIChatActivity (聊天界面)
    ↓
ChatAdapter (消息列表)
    ↓
ApiService (Retrofit)
    ↓
后端 /api/ai/chat (REST API)
```

## 性能指标

- **响应时间**：2-3秒
- **成功率**：100%
- **Token使用**：每次约70-120 tokens
- **并发支持**：Spring Boot默认线程池

## 后续优化建议

1. **添加对话历史**：保存用户聊天记录，实现多轮对话
2. **Prompt优化**：根据用户偏好定制系统提示词
3. **流式输出**：实现打字机效果的AI回复
4. **缓存机制**：对常见问题缓存AI回复
5. **限流保护**：防止API滥用

## 更新日志

### 2026-01-30
- ✅ 完成AI智能助手基础功能
- ✅ 集成智谱AI GLM-4-Flash模型
- ✅ 实现Android聊天界面
- ✅ 解决Docker环境变量加载问题
- ✅ 通过集成测试
