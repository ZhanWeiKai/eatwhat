# 今天吃什么 (What2Eat) - 点餐推送系统

一款面向朋友、情侣的社交点餐应用，解决"今天吃什么"的选择困难问题。用户可以浏览菜品、点菜并推送给好友，好友可以查看已推送的菜单列表，共同决定今天吃什么。

## ✨ 核心功能

- ✅ **用户注册/登录** - JWT认证，安全可靠
- ✅ **浏览菜品** - 按分类浏览（主食、蔬菜、肉类、海鲜、火锅、面食、汤类、饮品、甜品）
- ✅ **添加购物车** - 选择喜欢的菜品添加到购物车
- ✅ **推送菜单** - 一键推送菜单给好友
- ✅ **查看推送** - 查看好友推送的菜单列表（仅好友可见）
- ✅ **扫码添加好友** - 通过二维码添加好友（双向好友关系）
- ✅ **好友列表** - 查看好友列表，实时在线状态显示
- ✅ **分类管理** - 动态管理菜品分类，支持增删改查排序
- ✅ **上传菜品** - 用户可以上传新菜品（带图片），动态选择分类
- ✅ **权限控制** - 只能删除自己的推送和分类
- ✅ **好友可见性** - 只有好友才能看到彼此的推送
- ✅ **实时推送** - WebSocket实时接收菜单推送通知

## 📋 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.1 | Web框架 |
| JDK | 17 | Java版本 |
| MySQL | 8.0+ | 数据库 |
| Spring Data JPA | 3.2.1 | ORM框架 |
| Spring WebSocket | 3.2.1 | WebSocket支持 |
| JWT | 0.12.3 | Token认证 |
| BCrypt | - | 密码加密 |
| SpringDoc | 2.3.0 | API文档 |
| Maven | 3.9+ | 构建工具 |

### 前端（Android）
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | - | 开发语言 |
| Min SDK | 24 (Android 7.0) | 最低版本 |
| Target SDK | 34 (Android 14) | 目标版本 |
| Retrofit | 2.9.0 | 网络请求 |
| OkHttp | 4.12.0 | HTTP客户端 |
| Glide | 4.16.0 | 图片加载 |
| ZXing | 4.3.0 | 二维码扫描 |
| Gson | 2.10.1 | JSON解析 |
| CircleImageView | 3.1.0 | 圆形头像 |

## 🚀 快速开始

### 部署方式

本项目提供两种部署方式：

#### 方式1：Docker部署（推荐，生产环境）

详见部署文档：[DOCKER_DEPLOY.md](springboot-backend/DOCKER_DEPLOY.md)

**快速启动：**
```bash
# 1. 编译后端
cd springboot-backend
mvn clean package -DskipTests

# 2. 上传到服务器
scp target/what2eat-backend-1.0.0.jar root@YOUR_SERVER:/root/what2eat/target/
scp docker-compose.yml init_db.sql root@YOUR_SERVER:/root/what2eat/

# 3. 启动服务
ssh root@YOUR_SERVER
cd /root/what2eat
docker compose up -d

# 4. 查看日志
docker compose logs -f app
```

**服务器要求：**
- Docker 20.10+
- Docker Compose v2+
- 内存：至少2GB
- 端口：8883（应用）、3306（数据库）

#### 方式2：本地开发环境

### 环境要求

#### 后端环境
- **JDK 17+** - [下载地址](https://www.oracle.com/java/technologies/downloads/#java17)
- **Maven 3.9+** - [下载地址](https://maven.apache.org/download.cgi)
- **MySQL 8.0+** - [下载地址](https://dev.mysql.com/downloads/mysql/)
- **IntelliJ IDEA 2023.2+**（推荐）或 [VS Code](https://code.visualstudio.com/)

#### 前端环境
- **Android Studio Hedgehog (2023.1.1)+** - [下载地址](https://developer.android.com/studio)
- **JDK 17**
- **Android SDK 24+**
- **Android真机或模拟器**

---

## 📦 详细安装步骤

### 第一步：数据库配置

#### 1. 安装MySQL
确保已安装MySQL 8.0+，并启动MySQL服务。

**Windows启动MySQL服务：**
```bash
net start MySQL80
```

**Mac/Linux启动MySQL服务：**
```bash
sudo systemctl start mysql
# 或
sudo service mysql start
```

#### 2. 创建数据库
使用项目提供的SQL脚本初始化数据库：

**Windows:**
```bash
cd C:\claude-project\eatwhat
mysql -u root -p < init_database.sql
```

**Mac/Linux:**
```bash
cd /path/to/eatwhat
mysql -u root -p < init_database.sql
```

**手动创建（备选方案）：**
```sql
-- 登录MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE what2eat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE what2eat;

-- 创建用户表
CREATE TABLE user (
    user_id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100),
    avatar VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建菜品表
CREATE TABLE dish (
    dish_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(50),
    image_url VARCHAR(500),
    uploader_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (uploader_id) REFERENCES user(user_id)
);

-- 创建推送记录表
CREATE TABLE push (
    push_id VARCHAR(255) PRIMARY KEY,
    pusher_id VARCHAR(255) NOT NULL,
    pusher_name VARCHAR(100),
    pusher_avatar VARCHAR(500),
    dishes JSON,
    total_amount DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pusher_id) REFERENCES user(user_id)
);

-- 创建好友关系表
CREATE TABLE friendship (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    friend_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (friend_id) REFERENCES user(user_id),
    UNIQUE KEY unique_friendship (user_id, friend_id)
);
```

#### 3. 配置数据库连接
编辑 `springboot-backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/what2eat?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 123456  # 修改为您的MySQL密码
```

---

### 第二步：启动后端

#### 方式1：使用IntelliJ IDEA（推荐）

1. **打开项目**
   - 启动IntelliJ IDEA
   - 点击 `File` → `Open`
   - 选择 `springboot-backend` 目录
   - 等待Maven依赖下载完成

2. **配置运行**
   - 找到 `src/main/java/com/what2eat/What2EatApplication.java`
   - 右键点击文件 → `Run 'What2EatApplication'`
   - 或点击文件左侧的绿色运行按钮

3. **验证启动**
   - 控制台输出 `Started What2EatApplication` 表示启动成功
   - 访问 http://localhost:8883/api/swagger-ui.html 查看API文档

#### 方式2：使用Maven命令行

**Windows:**
```bash
cd springboot-backend
mvn spring-boot:run
```

**Mac/Linux:**
```bash
cd springboot-backend
./mvnw spring-boot:run
```

#### 方式3：使用启动脚本（Windows）

直接双击项目根目录的 `start-backend.bat` 文件。

#### 后端启动成功标志

看到以下输出表示启动成功：

```
========================================
   今天吃什么 - 后端服务启动成功
   API地址: http://localhost:8883/api/swagger-ui.html
   WebSocket: ws://localhost:8883/api/ws
========================================
```

**后端默认端口：8883**

---

### 第三步：配置并运行Android应用

#### 1. 打开项目

1. 启动Android Studio
2. 点击 `File` → `Open`
3. 选择 `android-app` 目录
4. 等待Gradle同步完成（首次可能需要几分钟）

#### 2. 配置后端地址

**重要：** 必须配置正确的后端地址才能正常使用。

##### 方案A：真机调试（推荐）

1. **获取电脑的局域网IP地址**

**Windows:**
```bash
ipconfig
# 查找 "无线局域网适配器 WLAN" 或 "以太网适配器" 下的 IPv4 地址
# 例如：192.168.1.100
```

**Mac:**
```bash
ifconfig
# 查找 en0 下的 inet 地址
```

2. **修改 build.gradle.kts**

编辑 `android-app/app/build.gradle.kts`，找到 `debug` 配置块：

```kotlin
buildTypes {
    debug {
        // 真机测试：修改为您的电脑局域网IP
        buildConfigField("String", "BASE_URL", "\"http://192.168.1.100:8883/api/\"")
        buildConfigField("String", "WS_URL", "\"ws://192.168.1.100:8883/api/ws\"")

        // 模拟器测试：使用10.0.2.2
        // buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8883/api/\"")
        // buildConfigField("String", "WS_URL", "\"ws://10.0.2.2:8883/api/ws\"")
    }
}
```

**替换 `192.168.1.100` 为您的实际IP地址！**

3. **同步Gradle**
   - 修改后点击顶部出现的 `Sync Now`
   - 或点击 `File` → `Sync Project with Gradle Files`

##### 方案B：模拟器调试

使用Android模拟器时，需要使用特殊IP `10.0.2.2`（这是模拟器访问主机的别名）：

```kotlin
buildTypes {
    debug {
        // 模拟器测试
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8883/api/\"")
        buildConfigField("String", "WS_URL", "\"ws://10.0.2.2:8883/api/ws\"")
    }
}
```

#### 3. 连接设备

**真机调试：**
1. 手机开启开发者选项
2. 启用USB调试
3. 用USB线连接电脑
4. 手机上允许USB调试授权
5. Android Studio顶部应显示设备型号

**模拟器调试：**
1. 在Android Studio中点击 `Tools` → `AVD Manager`
2. 创建或选择一个模拟器
3. 点击启动按钮

#### 4. 运行应用

1. 点击工具栏的绿色运行按钮 ▶️
2. 或按快捷键 `Shift + F10` (Windows/Linux) / `Control + R` (Mac)
3. 选择目标设备
4. 等待应用安装并启动

#### 5. 验证连接

应用启动后：
1. 打开注册页面
2. 输入用户名和密码，点击注册
3. 注册成功后自动登录
4. 进入主页说明连接成功！

---

## 📱 测试账号

项目预置了测试账号（如果运行了 `init_database.sql`）：

| 用户名 | 密码 | 昵称 |
|--------|------|------|
| testuser1 | 123456 | 测试用户1 |
| testuser2 | 123456 | 测试用户2 |
| testuser3 | 123456 | 测试用户3 |

---

## 🎯 功能演示流程

### 1. 添加好友流程

```
用户A                     用户B
  |                         |
  |--点击"我的二维码"        |
  |                         |
  |     显示二维码           |
  |                         |
  |          用户B扫描二维码 |
  |                         |
  |                         |--调用扫码功能
  |                         |
  |<---------解析出用户A的ID
  |                         |
  |                         |--点击"添加好友"
  |                         |
  |<----创建双向好友关系---->|
  |                         |
  |                         |
  ✓ 互相成为好友，可以查看对方的推送
```

### 2. 点菜推送流程

```
用户A
  |
  |--点击"开始点菜"
  |
  |--选择分类（热菜/凉菜等）
  |
  |--添加菜品到购物车
  |
  |--点击"推送菜单"
  |
  |--调用 /api/push
  |
  |--推送成功
  |
  ✓ 好友可以在推送列表看到
```

---

## 🔧 常见问题排查

### 问题1：后端启动失败

**症状：** 运行后报错 "Connection refused" 或 "Communications link failure"

**解决方法：**
1. 检查MySQL是否启动
   ```bash
   # Windows
   net start MySQL80

   # Mac/Linux
   sudo systemctl start mysql
   ```

2. 检查数据库是否创建
   ```bash
   mysql -u root -p
   SHOW DATABASES;
   # 确认存在 what2eat 数据库
   ```

3. 检查密码是否正确
   - 查看 `application.yml` 中的密码
   - 确认MySQL root用户密码一致

### 问题2：Android无法连接后端

**症状：** 网络请求失败，提示 "UnknownHostException" 或 "Connection refused"

**检查清单：**

✅ **后端是否启动？**
- 浏览器访问 http://localhost:8883/api/swagger-ui.html
- 能看到API文档说明后端正常

✅ **手机和电脑是否在同一WiFi？**
- 确保手机连接的WiFi和电脑相同
- 不要使用手机流量

✅ **IP地址是否正确？**
- Windows: 打开命令行执行 `ipconfig`
- Mac: 打开终端执行 `ifconfig`
- 找到IPv4地址（如 192.168.1.100）
- 确保 `build.gradle.kts` 中的IP与此一致

✅ **防火墙是否阻止？**
- Windows: 允许Java通过防火墙
  - 控制面板 → Windows Defender 防火墙 → 允许应用通过防火墙
  - 找到Java并勾选"专用"和"公用"

✅ **是否添加了网络权限？**
- 检查 `AndroidManifest.xml` 包含：
  ```xml
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
  ```

✅ **是否使用了明文流量？**
- 检查 `AndroidManifest.xml` application 标签包含：
  ```xml
  android:usesCleartextTraffic="true"
  ```

### 问题3：扫码功能无法使用

**症状：** 点击扫一扫后闪退或黑屏

**解决方法：**

1. **检查相机权限**
   - 设置 → 应用 → 今天吃什么 → 权限
   - 允许相机权限

2. **检查Min SDK版本**
   - 项目Min SDK为24，确保设备Android版本≥7.0

3. **清理重新编译**
   ```bash
   # Android Studio
   Build → Clean Project
   Build → Rebuild Project
   ```

### 问题4：图片上传失败

**症状：** 上传菜品时提示"上传失败"

**解决方法：**

1. **检查存储权限**
   - Android 13+需要 `READ_MEDIA_IMAGES` 权限
   - Android 6-12需要 `READ_EXTERNAL_STORAGE` 权限

2. **检查uploads目录**
   - 确保后端项目根目录存在 `uploads` 文件夹
   - 如果不存在，手动创建：
     ```bash
     cd springboot-backend
     mkdir uploads
     ```

3. **检查文件大小限制**
   - 默认最大10MB，超过会失败
   - 可在 `application.yml` 修改：
     ```yaml
     spring:
       servlet:
         multipart:
           max-file-size: 20MB
           max-request-size: 20MB
     ```

### 问题5：Maven依赖下载缓慢

**解决方法：**

**配置国内镜像** - 编辑 `~/.m2/settings.xml`：

```xml
<mirrors>
    <mirror>
        <id>aliyun</id>
        <mirrorOf>central</mirrorOf>
        <name>Aliyun Maven</name>
        <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
</mirrors>
```

---

## 📂 项目结构

```
eatwhat/
├── springboot-backend/              # Spring Boot 后端
│   ├── src/main/java/com/what2eat/
│   │   ├── controller/              # REST API 控制器
│   │   │   ├── AuthController.java      # 认证接口
│   │   │   ├── DishController.java      # 菜品接口
│   │   │   ├── PushController.java      # 推送接口
│   │   │   ├── FriendController.java    # 好友接口
│   │   │   └── FileController.java      # 文件上传接口
│   │   ├── service/                 # 业务逻辑层
│   │   │   ├── UserService.java
│   │   │   ├── DishService.java
│   │   │   ├── PushService.java
│   │   │   └── FriendService.java
│   │   ├── repository/              # 数据访问层
│   │   ├── entity/                  # 数据库实体
│   │   │   ├── User.java
│   │   │   ├── Dish.java
│   │   │   ├── Push.java
│   │   │   └── Friendship.java
│   │   ├── dto/                     # 数据传输对象
│   │   │   ├── request/
│   │   │   └── response/
│   │   ├── config/                  # 配置类
│   │   │   ├── WebSocketConfig.java
│   │   │   ├── SecurityConfig.java
│   │   │   └── CorsConfig.java
│   │   └── util/                    # 工具类
│   │       └── JwtUtil.java
│   ├── src/main/resources/
│   │   ├── application.yml          # 应用配置
│   │   └── uploads/                 # 文件上传目录
│   ├── pom.xml                      # Maven 配置
│   └── README.md
│
├── android-app/                     # Android 前端
│   ├── app/src/main/
│   │   ├── java/com/what2eat/
│   │   │   ├── ui/                  # UI 层
│   │   │   │   ├── login/           # 登录注册
│   │   │   │   ├── main/            # 主页
│   │   │   │   ├── menu/            # 菜单列表
│   │   │   │   ├── push/            # 推送列表
│   │   │   │   ├── qrcode/          # 二维码
│   │   │   │   └── upload/          # 上传菜品
│   │   │   ├── data/                # 数据层
│   │   │   │   ├── api/
│   │   │   │   │   ├── ApiService.java
│   │   │   │   │   └── ApiResponse.java
│   │   │   │   ├── model/
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Dish.java
│   │   │   │   │   ├── Push.java
│   │   │   │   │   └── Friendship.java
│   │   │   │   └── repository/
│   │   │   └── utils/               # 工具类
│   │   │       └── RetrofitClient.java
│   │   └── res/                     # 资源文件
│   │       ├── layout/              # 布局文件
│   │       ├── values/              # 资源值
│   │       ├── drawable/            # 图片资源
│   │       └── mipmap/              # 图标
│   ├── app/build.gradle.kts         # 应用构建配置
│   └── README.md
│
├── init_database.sql                # 数据库初始化脚本
├── start-backend.bat                # 后端启动脚本（Windows）
├── claude.md                        # Claude Code 配置
└── README.md                        # 本文件
```

---

## 🔑 核心代码说明

### 后端关键配置

#### 1. JWT Token配置
```yaml
jwt:
  secret: What2EatSecretKey2026ForJWTTokenGenerationMustBeLongEnough
  expiration: 604800000  # 7天
```

#### 2. 文件上传配置
```yaml
file:
  upload-dir: ./uploads
  base-url: http://10.88.1.127:8883/api  # 修改为您的IP
```

#### 3. 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/what2eat?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456  # 修改为您的密码
```

### Android关键配置

#### 1. 网络配置
```kotlin
buildConfigField("String", "BASE_URL", "\"http://YOUR_IP:8883/api/\"")
buildConfigField("String", "WS_URL", "\"ws://YOUR_IP:8883/api/ws\"")
```

#### 2. 权限配置
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

---

## 📊 数据库表设计

### user - 用户表
| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | VARCHAR(255) | 用户唯一标识（主键） |
| username | VARCHAR(50) | 用户名（唯一） |
| password | VARCHAR(255) | 密码（BCrypt加密） |
| nickname | VARCHAR(100) | 昵称 |
| avatar | VARCHAR(500) | 头像URL |
| online_status | INT | 在线状态（0=离线，1=在线） |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### dish_category - 菜品分类表
| 字段 | 类型 | 说明 |
|------|------|------|
| category_id | VARCHAR(255) | 分类唯一标识（主键） |
| name | VARCHAR(50) | 分类名称（唯一） |
| sort_order | INT | 排序字段 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### dish - 菜品表
| 字段 | 类型 | 说明 |
|------|------|------|
| dish_id | VARCHAR(255) | 菜品唯一标识（主键） |
| name | VARCHAR(100) | 菜品名称 |
| description | TEXT | 描述 |
| price | DECIMAL(10,2) | 价格 |
| category | VARCHAR(50) | 分类 |
| image_url | VARCHAR(500) | 图片URL |
| uploader_id | VARCHAR(255) | 上传者ID（外键） |

### push - 推送记录表
| 字段 | 类型 | 说明 |
|------|------|------|
| push_id | VARCHAR(255) | 推送唯一标识（主键） |
| pusher_id | VARCHAR(255) | 推送人ID（外键） |
| pusher_name | VARCHAR(100) | 推送人昵称 |
| pusher_avatar | VARCHAR(500) | 推送人头像 |
| dishes | JSON | 菜品列表（JSON格式） |
| total_amount | DECIMAL(10,2) | 总金额 |
| created_at | TIMESTAMP | 创建时间 |

### friendship - 好友关系表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(255) | 记录唯一标识（主键） |
| user_id | VARCHAR(255) | 用户ID（外键） |
| friend_id | VARCHAR(255) | 好友ID（外键） |
| created_at | TIMESTAMP | 创建时间 |

**注意：** 好友关系是**双向**的，添加好友时会创建两条记录。

---

## 🛠️ 开发指南

### 后端开发

#### 添加新的API接口
1. 在 `controller` 包创建新的Controller
2. 在 `service` 包创建对应的Service
3. 在 `repository` 包创建Repository（如需要）
4. 在 `entity` 包创建实体（如需要）

#### 修改数据库配置
编辑 `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/what2eat
    username: root
    password: YOUR_PASSWORD
```

#### 修改端口
编辑 `application.yml`:
```yaml
server:
  port: 8883  # 修改为其他端口
```

### Android开发

#### 添加新的Activity
1. 在 `ui` 包下创建新包
2. 创建Activity类
3. 创建对应的布局文件 `res/layout/activity_xxx.xml`
4. 在 `AndroidManifest.xml` 注册Activity

#### 添加新的API调用
1. 在 `ApiService.java` 添加接口定义
2. 创建对应的请求/响应模型
3. 在Activity中调用

#### 修改网络配置
编辑 `build.gradle.kts`:
```kotlin
buildConfigField("String", "BASE_URL", "\"http://YOUR_IP:8883/api/\"")
```

---

## 📈 API文档

后端启动后访问 Swagger UI：
```
http://localhost:8883/api/swagger-ui.html
```

### 主要接口

| 模块 | 路径 | 方法 | 说明 |
|------|------|------|------|
| **认证** | `/api/auth/register` | POST | 用户注册 |
| **认证** | `/api/auth/login` | POST | 用户登录（自动更新在线状态） |
| **认证** | `/api/auth/logout` | POST | 用户登出（自动更新离线状态） |
| **认证** | `/api/auth/me` | GET | 获取当前用户信息 |
| **菜品** | `/api/dishes` | GET | 获取菜品列表（按分类排序） |
| **菜品** | `/api/dishes` | POST | 上传菜品 |
| **菜品** | `/api/dishes/{id}` | GET | 获取菜品详情 |
| **菜品** | `/api/dishes/{id}` | DELETE | 删除菜品 |
| **菜品** | `/api/dishes/category/{category}` | GET | 按分类获取菜品 |
| **菜品** | `/api/dishes/categories/all` | GET | 获取所有分类名称 |
| **分类** | `/api/categories` | GET | 获取分类列表 |
| **分类** | `/api/categories` | POST | 创建分类 |
| **分类** | `/api/categories/{id}` | PUT | 更新分类 |
| **分类** | `/api/categories/{id}` | DELETE | 删除分类（需无菜品使用） |
| **推送** | `/api/push/list` | GET | 获取推送列表（仅好友） |
| **推送** | `/api/push/{id}` | GET | 获取推送详情 |
| **推送** | `/api/push` | POST | 创建推送 |
| **推送** | `/api/push/{id}` | DELETE | 删除推送 |
| **好友** | `/api/friends/list/{userId}` | GET | 获取好友列表（含在线状态） |
| **好友** | `/api/friends/add/{friendId}` | POST | 添加好友 |
| **好友** | `/api/friends/{friendId}` | DELETE | 删除好友 |
| **文件** | `/api/upload/image` | POST | 上传图片 |
| **WebSocket** | `/api/ws` | WS | WebSocket连接端点 |

---

## 🎨 技术亮点

1. **JWT认证** - 无状态Token认证，安全可靠，7天有效期
2. **密码加密** - BCrypt加密存储，符合安全规范
3. **好友关系** - 双向好友关系，确保互相可见
4. **在线状态** - 实时跟踪用户在线/离线状态，登录/登出自动更新
5. **权限控制** - 细粒度权限控制（只能删除自己的推送和分类）
6. **推送可见性** - 只有好友才能看到彼此的推送，保护隐私
7. **RESTful API** - 标准的REST接口设计
8. **文件上传** - 支持图片上传和URL访问，自动域名替换
9. **分类管理** - 动态分类系统，支持增删改查和排序
10. **菜品排序** - 按分类自动排序，提升浏览体验
11. **WebSocket推送** - 实时推送菜单和用户状态变更
12. **滚动联动** - 菜单页面左右分类联动，流畅交互
13. **Docker部署** - 容器化部署，一键启动完整服务栈
14. **数据库迁移** - Flyway自动化数据库版本管理
15. **模块化设计** - 前后端分离，易于维护和扩展

---

## 🐛 已知问题

- [ ] 暂不支持推送评论功能
- [ ] 暂不支持菜品搜索功能
- [ ] WebSocket推送功能已实现但部分场景未使用

---

## 📝 更新日志

### v1.2.0 (2026-01-21) - 分类管理与在线状态

**新功能：**
- ✨ 新增菜品分类管理功能（增删改查、排序）
- ✨ 新增好友列表页面，显示在线/离线状态
- ✨ 新增用户在线状态跟踪（登录/登出自动更新）
- ✨ 新增WebSocket实时推送（菜单推送、用户状态）
- ✨ 菜单页面滚动联动优化（左右分类自动同步）
- ✨ 菜品按分类自动排序显示

**优化：**
- 🎨 上传菜品页面分类动态加载，移除硬编码
- 🎨 菜单页面移除分类假数据，完全从服务器加载
- 🎨 优化分类删除逻辑，防止删除有菜品的分类
- 🎨 图片URL自动替换localhost和局域网IP为域名
- 🔒 代码优化：移除未使用的参数，改进异常处理

**部署：**
- 🐳 新增Docker Compose部署方案
- 🐳 新增数据库迁移脚本（Flyway）
- 🐳 迁移到新服务器（47.83.126.42）
- 📝 完善部署文档和开发指南

**Bug修复：**
- 🐛 修复好友列表返回按钮无响应问题
- 🐛 修复菜单滚动联动分类检测错误
- 🐛 修复推送可见性（只显示好友推送）

### v1.0.0 (2026-01-19)
- ✅ 完成用户认证模块
- ✅ 完成菜品管理模块
- ✅ 完成推送记录模块
- ✅ 完成好友关系模块（双向好友）
- ✅ 完成文件上传模块
- ✅ 修复推送可见性bug（只显示好友推送）
- ✅ 修复好友关系单向bug（现在创建双向关系）

---

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交Pull Request

---

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

---

## 👥 作者

**ZhanWeiKai** - [GitHub](https://github.com/ZhanWeiKai)

---

## 📧 联系方式

如有问题或建议，请提交Issue。

---

## ⭐ Star History

如果这个项目对您有帮助，请给个Star支持一下！

---

**项目开始日期：** 2026-01-15
**当前版本：** v1.2.0
**开发状态：** ✅ 功能完整，已部署生产环境
