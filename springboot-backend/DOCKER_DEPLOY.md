# Docker 部署指南

## 🚀 一键部署到Ubuntu服务器

### 前提条件
- 服务器已安装Docker和Docker Compose
- 已编译好的jar包在 `target/` 目录下

---

## 📋 部署步骤

### 1. 本地打包项目

在Windows本地执行：
```bash
cd springboot-backend
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd" clean package -DskipTests
```

### 2. 上传文件到服务器

将以下文件上传到服务器 `/root/what2eat/` 目录：
```
springboot-backend/
├── docker-compose.yml          # Docker编排文件
├── target/
│   └── what2eat-backend-1.0.0.jar  # 编译好的jar包
└── uploads/                        # 上传文件目录（可选）
```

上传命令（在本地PowerShell执行）：
```powershell
# 创建远程目录
ssh root@47.242.74.112 "mkdir -p /root/what2eat"

# 上传文件
scp .\docker-compose.yml root@47.242.74.112:/root/what2eat/
scp .\target\what2eat-backend-1.0.0.jar root@47.242.74.112:/root/what2eat/target/
scp -r .\uploads root@47.242.74.112:/root/what2eat/  # 如果有上传文件的话
```

### 3. 启动服务

SSH连接到服务器：
```bash
ssh root@47.242.74.112
```

进入项目目录：
```bash
cd /root/what2eat
```

启动所有服务：
```bash
docker compose up -d
```

### 4. 查看运行状态

```bash
# 查看容器状态
docker compose ps

# 查看应用日志
docker compose logs -f app

# 查看MySQL日志
docker compose logs -f mysql
```

---

## 🔧 常用命令

### 启动服务
```bash
docker compose up -d
```

### 停止服务
```bash
docker compose down
```

### 重启服务
```bash
docker compose restart
```

### 只重启Spring Boot应用
```bash
docker compose restart app
```

### 查看日志
```bash
# 查看所有日志
docker compose logs

# 实时查看应用日志
docker compose logs -f app

# 查看最近100行日志
docker compose logs --tail=100 app
```

### 进入容器
```bash
# 进入应用容器
docker compose exec app sh

# 进入MySQL容器
docker compose exec mysql bash
```

---

## 📊 服务信息

### Spring Boot应用
- **端口**：8883
- **访问地址**：http://47.242.74.112:8883/api
- **Swagger文档**：http://47.242.74.112:8883/api/swagger-ui.html

### MySQL数据库
- **端口**：3306
- **数据库名**：what2eat
- **用户名**：root
- **密码**：123456

---

## 🗄️ 数据库管理

### 连接到MySQL
```bash
# 从容器内连接
docker compose exec mysql mysql -uroot -p123456 what2eat

# 从服务器本地连接
mysql -h 127.0.0.1 -P 3306 -uroot -p123456 what2eat
```

### 备份数据
```bash
docker compose exec mysql mysqldump -uroot -p123456 what2eat > backup_$(date +%Y%m%d).sql
```

### 恢复数据
```bash
docker compose exec -T mysql mysql -uroot -p123456 what2eat < backup_20250120.sql
```

---

## 🐛 故障排查

### 1. 应用无法连接数据库
检查MySQL是否启动：
```bash
docker compose ps
```

查看MySQL日志：
```bash
docker compose logs mysql
```

### 2. 端口被占用
检查端口占用：
```bash
netstat -tlnp | grep 8883
netstat -tlnp | grep 3306
```

### 3. 重新构建
如果修改了代码：
```bash
# 1. 本地重新打包
mvn clean package -DskipTests

# 2. 上传新的jar包
scp .\target\what2eat-backend-1.0.0.jar root@47.242.74.112:/root/what2eat/target/

# 3. 重启应用
ssh root@47.242.74.112 "cd /root/what2eat && docker compose restart app"
```

### 4. 查看容器资源使用
```bash
docker stats
```

---

## ⚙️ 配置说明

### docker-compose.yml 关键配置

1. **MySQL服务**
   - 镜像：mysql:8.0
   - 数据持久化：通过 `volumes` 挂载到 `mysql-data`
   - 健康检查：确保MySQL完全启动后才启动应用

2. **Spring Boot应用**
   - 镜像：eclipse-temurin:17-jre-alpine（轻量级JRE）
   - 环境变量：覆盖数据库连接地址为 `mysql:3306`（容器名）
   - 依赖：等待MySQL健康检查通过后再启动

### 网络配置
- 两个容器在同一个网络 `what2eat-network` 中
- 应用可以通过容器名 `mysql` 访问数据库

---

## 🔐 安全建议

### 1. 修改默认密码
- 修改MySQL root密码
- 修改JWT密钥

### 2. 防火墙配置
```bash
# 只开放必要端口
ufw allow 8883/tcp  # API端口
ufw allow 22/tcp    # SSH
ufw enable
```

### 3. 定期备份数据
设置定时任务备份MySQL数据

---

## 📝 注意事项

1. **首次启动**：MySQL会自动创建数据库，JPA会自动创建表结构
2. **数据持久化**：MySQL数据存储在Docker volume `mysql-data` 中
3. **上传文件**：上传的文件会挂载到 `./uploads` 目录
4. **日志查看**：所有日志都可以通过 `docker compose logs` 查看

---

## 🎯 优势

✅ **一键部署**：不需要手动安装JDK和MySQL
✅ **环境隔离**：Docker容器隔离，不影响宿主机环境
✅ **易于维护**：通过docker-compose统一管理
✅ **数据安全**：数据持久化到volume
✅ **快速重启**：服务崩溃自动重启

---

## 📞 需要帮助？

如果遇到问题，检查：
1. Docker和Docker Compose是否正确安装
2. jar包是否正确编译
3. 端口是否被占用
4. 防火墙是否开放相应端口
