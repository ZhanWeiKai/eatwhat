# Claude Code 项目配置文件

## Maven 路径

Maven 执行路径:
```
C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd
```

## 常用命令

### 编译项目
```bash
cd springboot-backend
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd" compile
```

### 打包项目
```bash
cd springboot-backend
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd" package -DskipTests
```

### 启动后端服务
```bash
cd springboot-backend
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run
```

### 完整构建（测试+打包）
```bash
cd springboot-backend
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd" clean install
```

## 项目信息

- **后端端口**: 8883
- **本地 API 地址**: http://10.88.1.127:8883/api/
- **生产 API 地址**: http://api.jamesweb.org:8883/api/
- **WebSocket**: ws://api.jamesweb.org:8883/api/ws
- **Swagger UI**: http://api.jamesweb.org:8883/api/swagger-ui.html

## 数据库

- **数据库**: what2eat
- **端口**: 3306
- **初始化脚本**: init_db.sql

---

## 🚀 后端开发完整流程

### 标准开发流程（修复Bug/添加功能）

#### 步骤1：修改代码
- 修改或添加功能代码
- 检查逻辑是否正确
- 确保符合项目规范

#### 步骤2：验证代码编译
```bash
cd springboot-backend
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd" clean compile
```
- 检查编译是否成功
- 查看是否有警告或错误
- 检查代码是否有运行时错误的隐患（空指针、类型转换等）

#### 步骤3：本地打包测试
```bash
# 打包
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd" clean package -DskipTests

# 本地运行测试（可选）
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run
```
- 本地测试关键功能
- 验证修改是否生效
- 测试边界情况和异常处理

#### 步骤4：部署到服务器
```bash
# 上传JAR到服务器（SSH KEY认证，无需密码）
scp springboot-backend/target/what2eat-backend-1.0.0.jar root@47.83.126.42:/root/what2eat/target/

# 重启服务（SSH KEY认证，无需密码）
ssh root@47.83.126.42 "cd /root/what2eat && docker compose restart app"

# 等待启动（约5-10秒）
sleep 8
```

**服务器信息**：
- 服务器IP: `47.83.126.42`
- 用户名: `root`
- 项目目录: `/root/what2eat`
- Docker Compose文件: `/root/what2eat/docker-compose.yml`
- 服务器配置: 阿里云Linux 3, 3.5GB内存, Docker 26.1.3

**SSH KEY认证**：
- ✅ 已配置SSH KEY认证，无需输入密码
- 本地公钥路径: `~/.ssh/id_rsa.pub`
- 服务器授权文件: `~/.ssh/authorized_keys`
- 所有SSH操作自动执行，参考 `deploy-xiaozhi` skill配置方法

#### 步骤5：验证服务器部署
```bash
# 测试登录API
curl -s http://api.jamesweb.org:8883/api/auth/login \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser1","password":"123456"}'

# 查看应用日志（如有问题）
ssh root@47.83.126.42 "cd /root/what2eat && docker compose logs --tail=50 app"

# 查看容器状态
ssh root@47.83.126.42 "cd /root/what2eat && docker compose ps"
```

#### 步骤6：Git提交
```bash
# 查看修改
git status
git diff

# 添加修改的文件
git add .

# 提交（使用规范的commit message）
git commit -m "fix: 修复图片路径使用域名替代localhost

- 修改User.java、Dish.java、Push.java的getter方法
- 自动替换localhost和局域网IP为域名
- 修改application.yml中的base-url配置

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"

# 推送到GitHub
git push origin main
```

---

### 常见问题排查

#### 1. 编译失败
```bash
# 清理后重新编译
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd" clean compile
```

#### 2. Docker容器启动失败
```bash
# 查看容器日志
ssh root@47.83.126.42 "cd /root/what2eat && docker compose logs app"

# 重启容器
ssh root@47.83.126.42 "cd /root/what2eat && docker compose restart app"

# 完全重建（删除volume）
ssh root@47.83.126.42 "cd /root/what2eat && docker compose down -v && docker compose up -d"
```

#### 3. API无法访问
```bash
# 检查容器状态
ssh root@47.83.126.42 "docker ps"

# 检查端口监听
ssh root@47.83.126.42 "netstat -tlnp | grep 8883"

# 测试本地访问
ssh root@47.83.126.42 "curl -s http://localhost:8883/api/auth/login \
  -X POST \
  -H 'Content-Type: application/json' \
  -d '{\"username\":\"testuser1\",\"password\":\"123456\"}'"
```

---

### Android应用同步更新

当后端API修改后，如果影响Android应用，需要同步更新：

#### 1. 修改API配置
```kotlin
// android-app/app/build.gradle.kts
buildConfigField("String", "BASE_URL", "\"http://api.jamesweb.org:8883/api/\"")
buildConfigField("String", "WS_URL", "\"ws://api.jamesweb.org:8883/api/ws\"")
```

#### 2. 重新编译APK
```bash
cd android-app
./gradlew.bat clean assembleDebug
```

#### 3. 安装测试
```bash
# 连接手机后执行
adb install android-app\app\build\outputs\apk\debug\app-debug.apk
```

---

## 测试账号

- 用户名: `testuser1`
- 密码: `123456`
- 用户ID: `user001`
- 昵称: `测试用户1`

---

## 项目清理

### Claude Code临时文件管理

Claude Code在执行bash命令时会创建临时文件（`tmpclaude-*-cwd`），这些文件已自动整理到 `.claude-temp/` 文件夹。

**定期清理命令**：
```bash
# 清理所有Claude临时文件
rm -f tmpclaude-*-cwd nul

# 或移到临时文件夹
mkdir -p .claude-temp
mv tmpclaude-*-cwd .claude-temp/ 2>/dev/null
mv nul .claude-temp/ 2>/dev/null
```

**注意**：`.claude-temp/` 文件夹已在 `.gitignore` 中，不会被提交到git。
