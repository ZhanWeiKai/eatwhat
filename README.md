# 今天吃什么 - 点菜推送系统

一款面向朋友、情侣的菜品推荐应用，用户可以浏览菜品、点菜并推送给好友，好友可以查看已推送的菜单列表，共同决定今天吃什么。

## 项目概述

### 产品定位
"今天吃什么"是一款社交化的点菜应用，解决朋友、情侣、室友之间"吃什么"的选择困难问题。

### 核心功能

- ✅ **用户注册/登录**：账号密码登录，JWT认证
- ✅ **浏览菜品**：按分类浏览菜品（热菜、凉菜、主食、汤品等）
- ✅ **添加购物车**：选择喜欢的菜品添加到购物车
- ✅ **推送菜单**：一键推送菜单给好友
- ✅ **查看推送**：查看好友推送的菜单列表
- ✅ **权限控制**：只能删除自己的推送
- ✅ **扫码添加好友**：通过二维码添加好友
- ✅ **上传菜品**：用户可以上传新菜品
- ✅ **实时推送**：WebSocket实时推送新消息

## 技术栈

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
| Min SDK | 33 (Android 13) | 最低版本 |
| Target SDK | 33 | 目标版本 |
| Architecture | MVVM | 架构模式 |
| Retrofit | 2.9.0 | 网络请求 |
| OkHttp | 4.12.0 | HTTP客户端 |
| Glide | 4.16.0 | 图片加载 |
| ZXing | 4.3.0 | 二维码 |
| Gson | 2.10.1 | JSON解析 |

## 项目结构

```
test/
├── springboot-backend/             # Spring Boot后端
│   ├── src/main/java/com/what2eat/
│   │   ├── controller/            # REST API控制器
│   │   ├── service/               # 业务逻辑层
│   │   ├── repository/            # 数据访问层
│   │   ├── entity/                # 数据库实体
│   │   ├── dto/                   # 数据传输对象
│   │   ├── config/                # 配置类
│   │   └── util/                  # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml        # 应用配置
│   │   └── db/migration/          # 数据库脚本
│   ├── pom.xml                    # Maven配置
│   └── README.md                  # 后端说明
│
├── android-app/                   # Android前端
│   ├── app/src/main/
│   │   ├── java/com/what2eat/
│   │   │   ├── ui/                # UI层
│   │   │   ├── data/              # 数据层
│   │   │   └── utils/             # 工具类
│   │   └── res/                   # 资源文件
│   ├── app/build.gradle.kts       # 应用构建脚本
│   └── README.md                  # Android说明
│
├── docs/                          # 文档
│   └── API接口文档.md             # API文档
│
├── PRD-点菜系统.md                # 产品需求文档
├── 项目实施计划.md                # 实施计划
└── README.md                      # 本文件
```

## 快速开始

### 环境要求

#### 后端
- JDK 17+
- Maven 3.9+
- MySQL 8.0+

#### 前端
- Android Studio Hedgehog+
- JDK 17
- Android SDK 33+

### 1. 数据库配置

```bash
# 创建数据库
mysql -u root -p123456 < springboot-backend/src/main/resources/db/migration/V1__init_schema.sql
```

### 2. 启动后端

```bash
cd springboot-backend
mvn spring-boot:run
```

后端启动后访问：
- API地址：http://localhost:8883/api
- API文档：http://localhost:8883/api/swagger-ui.html
- WebSocket：ws://localhost:8883/api/ws

### 3. 配置Android

编辑 `android-app/app/build.gradle.kts`，修改后端地址：

```kotlin
debug {
    // 修改为您的电脑局域网IP
    buildConfigField("String", "BASE_URL", "\"http://192.168.1.100:8883/api/\"")
}
```

**获取局域网IP（Windows）：**
```bash
ipconfig
# 查找 "无线局域网适配器 WLAN" 下的 IPv4 地址
```

### 4. 运行Android

1. 打开Android Studio
2. 打开 `android-app` 目录
3. 连接Android设备（确保同一WiFi）
4. 点击 Run 按钮

## 测试账号

| 用户名 | 密码 | 昵称 |
|--------|------|------|
| testuser1 | 123456 | 测试用户1 |
| testuser2 | 123456 | 测试用户2 |
| testuser3 | 123456 | 测试用户3 |

## 主要功能演示

### 1. 登录流程

```
输入用户名和密码
  ↓
调用 /api/auth/login
  ↓
获取 Token 和用户信息
  ↓
保存到本地
  ↓
进入主页
```

### 2. 点菜推送流程

```
主页点击"开始点菜"
  ↓
选择分类（热菜/凉菜等）
  ↓
添加菜品到购物车
  ↓
点击"推送菜单"
  ↓
调用 /api/push
  ↓
WebSocket推送好友
  ↓
返回主页
```

### 3. 扫码添加好友流程

```
主页点击"我的二维码"
  ↓
生成包含userId的二维码
  ↓
好友扫描
  ↓
解析出userId
  ↓
调用 /api/friends/add
  ↓
添加成功
```

## API文档

详细的API文档请查看：[API接口文档.md](docs/API接口文档.md)

**主要接口：**

| 模块 | 接口 | 方法 | 说明 |
|------|------|------|------|
| 认证 | /auth/register | POST | 用户注册 |
| 认证 | /auth/login | POST | 用户登录 |
| 认证 | /auth/me | GET | 获取当前用户 |
| 菜品 | /dishes | GET | 获取菜品列表 |
| 菜品 | /dishes | POST | 上传菜品 |
| 推送 | /push/list | GET | 获取推送列表 |
| 推送 | /push | POST | 推送菜单 |
| 推送 | /push/{id} | DELETE | 删除推送 |
| 好友 | /friends | GET | 获取好友列表 |
| 好友 | /friends/add | POST | 添加好友 |
| 文件 | /upload/image | POST | 上传图片 |

## 数据库设计

### 用户表 (user)
- user_id: 用户唯一标识
- username: 用户名（唯一）
- password: 密码（加密）
- nickname: 昵称
- avatar: 头像URL

### 菜品表 (dish)
- dish_id: 菜品唯一标识
- name: 菜品名称
- description: 描述
- price: 价格
- category: 分类
- image_url: 图片URL
- uploader_id: 上传者ID

### 推送记录表 (push)
- push_id: 推送唯一标识
- pusher_id: 推送人ID
- pusher_name: 推送人昵称
- dishes: 菜品列表（JSON）
- total_amount: 总金额

### 好友关系表 (friendship)
- id: 记录唯一标识
- user_id: 用户ID
- friend_id: 好友ID

## 开发进度

### 后端（Spring Boot）

- [x] 用户认证模块（注册/登录/JWT）
- [x] 菜品管理模块
- [x] 推送记录模块
- [x] 好友关系模块
- [x] 文件上传模块
- [x] WebSocket实时推送
- [x] 跨域配置
- [x] API文档

### 前端（Android）

- [x] 项目结构搭建
- [x] 网络层配置
- [x] 数据模型定义
- [x] API接口定义
- [ ] 登录/注册UI
- [ ] 主页UI
- [ ] 点菜功能
- [ ] 推送记录功能
- [ ] 二维码功能
- [ ] 上传菜品功能
- [ ] WebSocket实时推送

## 常见问题

### Q1: Android无法连接后端？

**检查清单：**
1. 后端是否启动（访问 http://localhost:8883/api）
2. 手机和电脑是否在同一WiFi
3. IP地址是否正确（使用 `ipconfig` 查看）
4. 是否添加了网络权限
5. 是否设置了 `usesCleartextTraffic="true"`

### Q2: 数据库连接失败？

**解决方法：**
1. 检查MySQL是否启动
2. 检查密码是否为 `123456`
3. 检查数据库是否创建成功
4. 查看 `application.yml` 配置

### Q3: WebSocket连接失败？

**解决方法：**
1. 检查后端是否支持WebSocket
2. 检查URL是否正确（`ws://192.168.x.x:8883/api/ws`）
3. 检查手机和电脑是否在同一网络

## 技术亮点

1. **JWT认证**：无状态Token认证，安全可靠
2. **密码加密**：BCrypt加密存储，安全规范
3. **权限控制**：细粒度权限控制（只能删除自己的推送）
4. **实时推送**：WebSocket实时推送，体验流畅
5. **RESTful API**：标准的REST接口设计
6. **MVVM架构**：Android采用MVVM架构，代码清晰
7. **模块化设计**：前后端分离，易于维护

## 后续优化

- [ ] 实现Android完整UI
- [ ] 添加消息通知功能
- [ ] 优化图片加载性能
- [ ] 添加菜品搜索功能
- [ ] 实现推送评论功能
- [ ] 添加数据统计功能
- [ ] 优化UI交互体验

## 相关文档

- [产品需求文档](PRD-点菜系统.md)
- [项目实施计划](项目实施计划.md)
- [API接口文档](docs/API接口文档.md)
- [后端README](springboot-backend/README.md)
- [Android README](android-app/README.md)

## 许可证

MIT License

## 联系方式

如有问题，请提交Issue。

---

**项目开始日期**：2026-01-15
**版本**：v1.0.0
**开发团队**：What2Eat Team
# eatwhat
