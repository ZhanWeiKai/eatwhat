# 今天吃什么 - Docker部署完整步骤文档

## 📅 部署时间
**部署日期**：2026年1月20日
**部署人员**：Claude AI Assistant
**服务器IP**：47.242.74.112（中国香港）
**操作系统**：Ubuntu 22.04 LTS

---

## 🎯 部署目标
- 使用Docker Compose部署Spring Boot后端和MySQL数据库
- 避免手动安装JDK和MySQL环境
- 实现一键启动和管理

---

## 📋 部署前准备

### 服务器环境
- ✅ Docker已安装（版本 29.1.5）
- ✅ Docker Compose已安装（版本 v5.0.1）
- ✅ Node.js已安装（版本 v24.13.0）
- ✅ 免费额度：300元，剩余充足
- ✅ 磁盘空间：40GB，使用率9%

### 本地环境
- Windows系统
- Maven路径：`C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd`
- 项目路径：`C:\claude-project\eatwhat\`

---

## 🚀 完整部署步骤

### **步骤1：在服务器上创建项目目录**

**执行命令**：
```bash
ssh root@47.242.74.112
mkdir -p /root/what2eat/target
mkdir -p /root/what2eat/uploads
```

**操作说明**：
- 创建主目录 `/root/what2eat`
- 创建子目录 `target/`（存放jar包）
- 创建子目录 `uploads/`（存放上传文件）

**执行结果**：
```
total 16
drwxr-xr-x  4 root root 4096 Jan 20 11:31 .
drwx------ 14 root root 4096 Jan 20 11:31 ..
drwxr-xr-x  2 root root 4096 Jan 20 11:31 target
drwxr-xr-x  2 root root 4096 Jan 20 11:31 uploads
```

---

### **步骤2：上传docker-compose.yml**

**本地文件**：`C:\claude-project\eatwhat\springboot-backend\docker-compose.yml`

**执行命令**：
```powershell
scp "C:\claude-project\eatwhat\springboot-backend\docker-compose.yml" root@47.242.74.112:/root/what2eat/
```

**文件内容**：
```yaml
version: '3.8'

services:
  # MySQL数据库
  mysql:
    image: mysql:8.0
    container_name: what2eat-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: 123456
      MYSQL_DATABASE: what2eat
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      # 数据持久化
      - mysql-data:/var/lib/mysql
      # 初始化SQL脚本
      - ./init_db.sql:/docker-entrypoint-initdb.d/init_db.sql
    networks:
      - what2eat-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-p123456"]
      timeout: 20s
      retries: 10

  # Spring Boot应用
  app:
    # 使用官方OpenJDK 17镜像
    image: eclipse-temurin:17-jre-alpine
    container_name: what2eat-app
    restart: always
    working_dir: /app
    environment:
      # 数据库连接配置（连接到MySQL容器）
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/what2eat?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: 123456
      TZ: Asia/Shanghai
    ports:
      - "8883:8883"
    volumes:
      # 挂载jar包
      - ./target/what2eat-backend-1.0.0.jar:/app/app.jar
      # 挂载上传文件目录
      - ./uploads:/app/uploads
    networks:
      - what2eat-network
    depends_on:
      mysql:
        condition: service_healthy
    command: ["java", "-jar", "app.jar"]

# 数据卷
volumes:
  mysql-data:
    driver: local

# 网络
networks:
  what2eat-network:
    driver: bridge
```

**执行结果**：
- ✅ 文件大小：1.6KB
- ✅ 上传成功：`/root/what2eat/docker-compose.yml`

---

### **步骤3：本地打包Spring Boot项目**

**执行命令**：
```bash
cd C:\claude-project\eatwhat\springboot-backend
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd" clean package -DskipTests
```

**打包过程**：
```
[INFO] Scanning for projects...
[INFO] Building What2Eat Backend 1.0.0
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] --- clean:3.3.2:clean (default-clean) @ what2eat-backend ---
[INFO] Deleting C:\claude-project\eatwhat\springboot-backend\target
[INFO] --- resources:3.3.1:resources (default-resources) @ what2eat-backend ---
[INFO] Copying 1 resource from src\main\resources to target\classes
[INFO] --- compiler:3.11.0:compile (default-compile) @ what2eat-backend ---
[INFO] Compiling 26 source files with javac [debug release 17] to target\classes
[INFO] --- jar:3.3.0:jar (default-jar) @ what2eat-backend ---
[INFO] Building jar: C:\claude-project\eatwhat\springboot-backend\target\what2eat-backend-1.0.0.jar
[INFO] --- spring-boot:3.2.1:repackage (repackage) @ what2eat-backend ---
[INFO] Replacing main artifact with repackaged archive
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.978 s
```

**执行结果**：
- ✅ 构建成功：BUILD SUCCESS
- ⏱️ 耗时：3.978秒
- 📦 生成文件：`target/what2eat-backend-1.0.0.jar`
- 📝 包含26个源文件
- 💾 文件大小：55MB

---

### **步骤4：上传jar包到服务器**

**执行命令**：
```powershell
scp "C:\claude-project\eatwhat\springboot-backend\target\what2eat-backend-1.0.0.jar" root@47.242.74.112:/root/what2eat/target/
```

**执行结果**：
```
total 55M
-rw-r--r-- 1 root root 55M Jan 20 11:36 what2eat-backend-1.0.0.jar
```

- ✅ 文件大小：55MB
- ✅ 文件类型：Java archive data (JAR)
- ✅ 服务器位置：`/root/what2eat/target/what2eat-backend-1.0.0.jar`

---

### **步骤5：上传deploy.sh脚本**

**本地文件**：`C:\claude-project\eatwhat\springboot-backend\deploy.sh`

**执行命令**：
```powershell
scp "C:\claude-project\eatwhat\springboot-backend\deploy.sh" root@47.242.74.112:/root/what2eat/
chmod +x /root/what2eat/deploy.sh
```

**执行结果**：
```
-rwxr-xr-x 1 root root 2.0K Jan 20 11:36 deploy.sh
```

- ✅ 文件大小：2.0K
- ✅ 权限：可执行（755）

---

### **步骤6：创建并上传数据库初始化SQL文件**

**问题**：之前发现 `init_db.sql` 被Docker创建成了目录而不是文件

**解决方案**：
1. 创建新的 `init_db.sql` 文件（使用 `CREATE TABLE IF NOT EXISTS` 和 `INSERT IGNORE`）
2. 删除服务器上错误的目录
3. 上传正确的文件

**创建新文件**：
- 文件路径：`C:\claude-project\eatwhat\springboot-backend\init_db.sql`
- 文件内容：包含建表语句和测试数据插入语句
- 使用 `CREATE TABLE IF NOT EXISTS` 避免重复创建
- 使用 `INSERT IGNORE` 避免重复插入

**删除错误的目录**：
```bash
ssh root@47.242.74.112 "rm -rf /root/what2eat/init_db.sql"
```

**上传正确的文件**：
```powershell
scp "C:\claude-project\eatwhat\springboot-backend\init_db.sql" root@47.242.74.112:/root/what2eat/"
```

**执行结果**：
```
-rw-r--r-- 1 root root 4.0K Jan 20 11:42 /root/what2eat/init_db.sql
```

- ✅ 文件大小：4.0K
- ✅ 文件类型：普通文件（不再是目录）

**文件内容概要**：
```sql
-- 用户表
CREATE TABLE IF NOT EXISTS user (...)
-- 菜品表
CREATE TABLE IF NOT EXISTS dish (...)
-- 推送记录表
CREATE TABLE IF NOT EXISTS push (...)
-- 好友关系表
CREATE TABLE IF NOT EXISTS friendship (...)

-- 插入3个测试用户
INSERT IGNORE INTO user ...

-- 插入10个测试菜品
INSERT IGNORE INTO dish ...

-- 插入2条测试推送
INSERT IGNORE INTO push ...
```

---

### **步骤7：首次启动Docker服务**

**执行命令**：
```bash
ssh root@47.242.74.112 "cd /root/what2eat && docker compose up -d"
```

**执行过程**：
```
time="2026-01-20T11:43:40+08:00" level=warning msg="No services to build"
 Network what2eat_what2eat-network Creating
 Network what2eat_what2eat-network Created
 Container what2eat-mysql Creating
 Container what2eat-mysql Created
 Container what2eat-app Creating
 Container what2eat-app Created
 Container what2eat-mysql Starting
 Container what2eat-mysql Started
 Container what2eat-mysql Waiting
 Container what2eat-mysql Healthy
 Container what2eat-app Starting
 Container what2eat-app Started
```

**执行结果**：
- ✅ 网络创建成功：`what2eat_what2eat-network`
- ✅ 数据卷创建成功：`what2eat_mysql-data`
- ✅ MySQL容器创建并启动
- ✅ MySQL健康检查通过
- ✅ Spring Boot应用创建并启动

---

### **步骤8：检查服务运行状态**

**执行命令**：
```bash
ssh root@47.242.74.112 "cd /root/what2eat && docker compose ps"
```

**执行结果**：
```
NAME             IMAGE                           COMMAND                  SERVICE   CREATED              STATUS                    PORTS
what2eat-app     eclipse-temurin:17-jre-alpine   "/__cacert_entrypoin…"   app       About a minute ago   Up 26 seconds             0.0.0.0:8883->8883/tcp, [::]:8883->8883/tcp
what2eat-mysql   mysql:8.0                       "docker-entrypoint.s…"   mysql     About a minute ago   Up 57 seconds (healthy)   0.0.0.0:3306->3306/tcp, [::]:3306->3306/tcp, 33060/tcp
```

**状态说明**：
- ✅ MySQL容器：健康状态（healthy），运行57秒
- ✅ Spring Boot应用：运行正常，运行26秒
- ✅ 端口映射：MySQL 3306、应用 8883

---

### **步骤9：检查应用日志**

**执行命令**：
```bash
ssh root@47.242.74.112 "docker logs what2eat-app 2>&1 | tail -20"
```

**关键日志输出**：
```
Hibernate: alter table user add constraint UK_sb8bbouer5wak8vyiiy4pf2bx unique (username)
2026-01-20 11:44:21 - Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-01-20 11:44:22 - Hibernate is in classpath; If applicable, HQL parser will be used.
2026-01-20 11:44:23 - Tomcat started on port 8883 (http) with context path '/api'
2026-01-20 11:44:23 - Starting...
2026-01-20 11:44:23 - BrokerAvailabilityEvent[available=true]
2026-01-20 11:44:23 - Started What2EatApplication in 10.534 seconds (process running for 11.797)

========================================
   今天吃什么 - 后端服务启动成功！
   API文档: http://localhost:8883/api/swagger-ui.html
   WebSocket: ws://localhost:8883/api/ws
========================================
```

**日志分析**：
- ✅ JPA/Hibernate初始化成功
- ✅ 数据库约束创建成功
- ✅ Tomcat在8883端口启动
- ✅ 应用启动完成（耗时10.5秒）
- ✅ WebSocket已就绪

---

### **步骤10：验证数据库初始化（发现数据为空）**

**执行命令**：
```bash
ssh root@47.242.74.112 "docker exec what2eat-mysql mysql -uroot -p123456 -e 'USE what2eat; SELECT COUNT(*) as count FROM user; SELECT COUNT(*) as count FROM dish;'"
```

**执行结果**：
```
count
0
count
0
```

**问题分析**：
- ❌ 数据库表已创建，但数据为空
- 原因：Docker volume在之前失败的启动中已经创建
- Docker机制：只有首次创建volume时才会执行初始化脚本
- 结果：`init_db.sql` 没有被执行

---

### **步骤11：删除volume并重新初始化**

**执行命令**：
```bash
ssh root@47.242.74.112 "cd /root/what2eat && docker compose down -v"
```

**执行过程**：
```
Container what2eat-app Stopping
Container what2eat-app Stopped
Container what2eat-app Removing
Container what2eat-mysql Stopping
Container what2eat-mysql Stopped
Container what2eat-mysql Removing
Network what2eat_what2eat-network Removing
Volume what2eat_mysql-data Removing
Volume what2eat_mysql-data Removed
```

**操作说明**：
- 停止所有容器
- 删除所有容器
- 删除网络
- **删除数据卷（重要：-v 参数）**

**结果**：
- ✅ 所有资源清理完毕
- ✅ MySQL数据卷已删除
- ✅ 下次启动将重新执行初始化脚本

---

### **步骤12：重新启动服务（第二次）**

**执行命令**：
```bash
ssh root@47.242.74.112 "cd /root/what2eat && docker compose up -d"
```

**执行过程**：
```
time="2026-01-20T11:49:30+08:00" level=warning msg="No services to build"
 Network what2eat_what2eat-network Creating
 Network what2eat_what2eat-network Created
 Volume what2eat_mysql-data Creating
 Volume what2eat_mysql-data Created
 Container what2eat-mysql Creating
 Container what2eat-mysql Created
 Container what2eat-app Creating
 Container what2eat-app Created
 Container what2eat-mysql Started
 Container what2eat-mysql Started
 Container what2eat-mysql Waiting
 Container what2eat-mysql Healthy
 Container what2eat-app Starting
 Container what2eat-app Started
```

**操作说明**：
- 创建新的数据卷
- MySQL容器会自动执行 `/docker-entrypoint-initdb.d/init_db.sql`
- Spring Boot应用等待MySQL健康检查通过后再启动

---

### **步骤13：等待MySQL初始化完成（30秒）**

**执行命令**：
```bash
sleep 30 && ssh root@47.242.74.112 "docker exec what2eat-mysql mysql -uroot -p123456 -e 'USE what2eat; SELECT COUNT(*) as user_count FROM user; SELECT COUNT(*) as dish_count FROM dish;'"
```

**执行结果**：
```
user_count
3
dish_count
10
```

**✅ 数据初始化成功！**
- 3个测试用户已插入
- 10个测试菜品已插入

---

### **步骤14：验证详细数据**

**执行命令**：
```bash
ssh root@47.242.74.112 "docker exec what2eat-mysql mysql -uroot -p123456 --default-character-set=utf8mb4 -e 'USE what2eat; SELECT username, nickname FROM user; SELECT name, price, category FROM dish LIMIT 5;'"
```

**执行结果**：
```
username	nickname
testuser1	测试用户1
testuser2	测试用户2
testuser3	测试用户3

name	price	category
麻婆豆腐	28.00	热菜
宫保鸡丁	38.00	热菜
红烧肉	45.00	热菜
糖醋排骨	42.00	热菜
清炒时蔬	18.00	凉菜
```

**推送记录**：
```bash
ssh root@47.242.74.112 "docker exec what2eat-mysql mysql -uroot -p123456 -e 'USE what2eat; SELECT push_id, pusher_name, total_amount FROM push;'"
```

**结果**：
```
push_id	pusher_name	total_amount
push001	测试用户1	104.00
push002	测试用户2	87.00
```

**✅ 所有测试数据验证成功！**

---

### **步骤15：测试API登录功能**

**执行命令**：
```bash
ssh root@47.242.74.112 "curl -s 'http://localhost:8883/api/auth/login' -X POST -H 'Content-Type: application/json' -d '{\"username\":\"testuser1\",\"password\":\"123456\"}'"
```

**执行结果**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "nickname": "测试用户1",
    "avatar": "http://localhost:8883/api/static/default-avatar.png",
    "userId": "user001",
    "token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ1c2VyMDAxIiwiaWF0IjoxNzY4ODgxMTE4LCJleHAiOjE3Njk0ODU5MTh9.-_fG-fHVgwqFUvm6RypdGfIl-ua5I8UIE7-4uLi2EbgK5sev0KSG8BFlln9PwgVl",
    "username": "testuser1"
  }
}
```

**✅ API登录测试成功！**
- 返回状态码：200
- 用户信息正确
- JWT Token已生成

---

## 📊 部署后的服务器状态

### **服务器基本信息**
```
IP地址: 47.242.74.112
地域: 中国香港
操作系统: Ubuntu 22.04 LTS
内核版本: 5.15.0-164-generic
```

### **磁盘使用情况**
```
总容量: 40GB
已使用: 5.2GB
可用空间: 33GB
使用率: 13%
```

### **Docker镜像**
```
eclipse-temurin:17-jre-alpine   (Spring Boot应用)
mysql:8.0                       (MySQL数据库)
```

### **Docker容器**
```
what2eat-mysql    MySQL 8.0     端口: 3306   状态: healthy
what2eat-app     Spring Boot    端口: 8883   状态: running
```

### **数据卷**
```
what2eat_mysql-data    (MySQL数据持久化)
```

### **网络**
```
what2eat_what2eat-network    (bridge网络)
```

---

## 🌐 服务访问信息

### **后端API**
- **访问地址**：http://47.242.74.112:8883/api
- **Swagger文档**：http://47.242.74.112:8883/api/swagger-ui.html
- **WebSocket**：ws://47.242.74.112:8883/api/ws

### **MySQL数据库**
- **端口**：3306
- **数据库名**：what2eat
- **用户名**：root
- **密码**：123456
- **字符集**：utf8mb4

### **测试账号**
```
用户名: testuser1
密码: 123456
昵称: 测试用户1

用户名: testuser2
密码: 123456
昵称: 测试用户2

用户名: testuser3
密码: 123456
昵称: 测试用户3
```

---

## 📁 服务器文件结构

```
/root/what2eat/
├── docker-compose.yml              (1.6K) Docker编排文件
├── init_db.sql                     (4.0K) 数据库初始化脚本
├── deploy.sh                       (2.0K) 快速部署脚本（可执行）
├── target/
│   └── what2eat-backend-1.0.0.jar  (55M)  Spring Boot应用jar包
└── uploads/                        (空目录，用于存放上传文件)
```

---

## 🎯 数据库表结构

### **已创建的表**
1. **user**（用户表）- 3条记录
2. **dish**（菜品表）- 10条记录
3. **push**（推送记录表）- 2条记录
4. **friendship**（好友关系表）- 0条记录

### **测试数据详情**

**用户数据**：
- testuser1（测试用户1）
- testuser2（测试用户2）
- testuser3（测试用户3）
- 所有用户密码：123456

**菜品数据**：
1. 麻婆豆腐 - ¥28.00 - 热菜
2. 宫保鸡丁 - ¥38.00 - 热菜
3. 红烧肉 - ¥45.00 - 热菜
4. 糖醋排骨 - ¥42.00 - 热菜
5. 清炒时蔬 - ¥18.00 - 凉菜
6. 口水鸡 - ¥32.00 - 凉菜
7. 扬州炒饭 - ¥25.00 - 主食
8. 牛肉面 - ¥28.00 - 主食
9. 西红柿鸡蛋汤 - ¥15.00 - 汤品
10. 银耳莲子汤 - ¥18.00 - 汤品

**推送记录**：
- push001（测试用户1）- 总价 ¥104.00
- push002（测试用户2）- 总价 ¥87.00

---

## 🔧 常用运维命令

### **部署相关**
```bash
# SSH连接服务器
ssh root@47.242.74.112

# 进入项目目录
cd /root/what2eat

# 启动服务
docker compose up -d

# 停止服务
docker compose down

# 重启服务
docker compose restart

# 停止并删除数据卷（危险！）
docker compose down -v

# 查看服务状态
docker compose ps

# 查看所有日志
docker compose logs

# 查看应用日志
docker compose logs -f app

# 查看MySQL日志
docker compose logs -f mysql
```

### **使用deploy.sh脚本**
```bash
# 部署或更新
./deploy.sh deploy

# 重启服务
./deploy.sh restart

# 停止服务
./deploy.sh stop

# 查看日志
./deploy.sh logs

# 查看应用日志
./deploy.sh logs-app

# 查看MySQL日志
./deploy.sh logs-mysql

# 查看状态
./deploy.sh status
```

### **数据库管理**
```bash
# 连接到MySQL
docker exec -it what2eat-mysql mysql -uroot -p123456 what2eat

# 查看所有表
docker exec what2eat-mysql mysql -uroot -p123456 -e 'USE what2eat; SHOW TABLES;'

# 查看用户数据
docker exec what2eat-mysql mysql -uroot -p123456 -e 'USE what2eat; SELECT * FROM user;'

# 查看菜品数据
docker exec what2eat-mysql mysql -uroot -p123456 -e 'USE what2eat; SELECT * FROM dish;'

# 备份数据库
docker exec what2eat-mysql mysqldump -uroot -p123456 what2eat > backup_$(date +%Y%m%d).sql

# 恢复数据库
docker exec -i what2eat-mysql mysql -uroot -p123456 what2eat < backup_20250120.sql
```

### **容器管理**
```bash
# 查看所有容器
docker ps -a

# 查看容器资源使用
docker stats

# 进入应用容器
docker exec -it what2eat-app sh

# 进入MySQL容器
docker exec -it what2eat-mysql bash

# 查看应用日志
docker logs what2eat-app

# 查看MySQL日志
docker logs what2eat-mysql
```

---

## ⚠️ 重要注意事项

### **1. 免费额度管理**
- **总免费额度**：300元
- **每小时上限**：0.833元
- **试用期**：3个月
- **到期日期**：2026年4月20日

**监控方式**：
```bash
# 登录阿里云ECS控制台查看免费额度
# 网址: https://ecs.console.aliyun.com
# 路径: 概览 → 我的试用进度
```

**到期前操作**：
- ✅ 继续使用：转为按量付费或包年包月
- ✅ 不再使用：释放实例避免扣费
- ⚠️ **重要**：到期后实例不会自动停止，会继续按量收费

### **2. 数据持久化**
- MySQL数据存储在Docker volume `what2eat_mysql-data` 中
- 即使容器删除，数据也不会丢失
- 只有执行 `docker compose down -v` 才会删除数据

### **3. 密码安全**
- ⚠️ **建议修改**：MySQL root密码（当前：123456）
- ⚠️ **建议修改**：JWT密钥（当前在代码中明文存储）

### **4. 端口说明**
- **8883**：Spring Boot API端口（已对外开放）
- **3306**：MySQL端口（已对外开放，建议仅限内网）
- **33060**：MySQL X Protocol端口

### **5. 更新应用**
```bash
# 本地重新打包
mvn clean package -DskipTests

# 上传新的jar包
scp .\target\what2eat-backend-1.0.0.jar root@47.242.74.112:/root/what2eat/target/

# 重启应用容器
ssh root@47.242.74.112 "cd /root/what2eat && docker compose restart app"
```

### **6. 故障排查**

**应用无法启动**：
```bash
# 查看应用日志
docker compose logs app

# 检查数据库连接
docker compose logs mysql

# 验证MySQL是否健康
docker compose ps
```

**API无法访问**：
```bash
# 检查容器状态
docker compose ps

# 检查端口是否开放
netstat -tlnp | grep 8883

# 测试本地访问
curl http://localhost:8883/api/auth/login
```

**数据丢失**：
```bash
# 检查volume状态
docker volume ls | grep mysql

# 查看volume详情
docker volume inspect what2eat_mysql-data
```

---

## 📱 Android APP配置

### **修改API地址**

**文件位置**：
```
android-app/app/src/main/java/com/what2eat/utils/RetrofitClient.java
```

**修改内容**：
```java
private static final String BASE_URL = "http://47.242.74.112:8883/api/";
```

**注意**：
- 将 `10.88.1.127` 改为 `47.242.74.112`
- 重新编译Android APP

---

## 🎉 部署成功验证清单

- ✅ 服务器目录创建成功
- ✅ docker-compose.yml上传成功
- ✅ Spring Boot项目打包成功
- ✅ jar包上传成功
- ✅ deploy.sh脚本上传成功
- ✅ init_db.sql创建并上传成功
- ✅ Docker服务启动成功
- ✅ MySQL容器健康状态
- ✅ Spring Boot应用运行正常
- ✅ 数据库初始化成功
- ✅ 测试数据插入成功（3用户，10菜品，2推送）
- ✅ API登录测试成功
- ✅ JWT Token生成正常

---

## 📝 部署总结

### **部署耗时**
- 准备工作：已完成（Docker、Node.js等已安装）
- 文件上传：约5分钟
- 服务启动：约3分钟（包括volume重新初始化）
- 数据验证：约2分钟
- **总计**：约10分钟

### **部署方式**
- ✅ 使用Docker Compose
- ✅ 多容器应用编排
- ✅ 数据持久化到volume
- ✅ 健康检查确保启动顺序
- ✅ 自动重启策略

### **部署优势**
- ✅ 无需手动安装JDK和MySQL
- ✅ 环境隔离，不影响宿主机
- ✅ 一键启动，易于维护
- ✅ 数据安全，持久化存储
- ✅ 易于备份和恢复

### **关键经验**
1. **Docker初始化脚本只在volume首次创建时执行**
   - 如果volume已存在，需要用 `docker compose down -v` 删除后再重建

2. **健康检查很重要**
   - 确保MySQL完全启动后再启动应用
   - 避免应用连接失败

3. **字符集问题**
   - MySQL使用utf8mb4字符集
   - 初始化SQL文件开头设置字符集

4. **API路径规范**
   - 注意context-path：`/api`
   - Controller的@RequestMapping：如`/auth`
   - 完整路径：`/api/auth/login`

---

## 📞 需要帮助？

**阿里云资源**：
- ECS控制台：https://ecs.console.aliyun.com
- 免费试用：https://free.aliyun.com

**本地项目**：
- 项目路径：`C:\claude-project\eatwhat`
- 后端路径：`springboot-backend`
- Android路径：`android-app`

**服务器连接**：
```bash
ssh root@47.242.74.112
```

---

**部署完成时间**：2026年1月20日 11:51
**文档版本**：v1.0
**部署状态**：✅ 成功
