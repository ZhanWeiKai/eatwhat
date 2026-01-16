# 今天吃什么 - Spring Boot后端

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0+

### 2. 数据库配置

1. 创建数据库：
```bash
mysql -u root -p
```

2. 执行初始化脚本：
```bash
mysql -u root -p < src/main/resources/db/migration/V1__init_schema.sql
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库密码（如果不是123456）：
```yaml
spring:
  datasource:
    password: 你的密码
```

### 4. 启动项目

```bash
# 方式1：使用Maven
mvn spring-boot:run

# 方式2：使用IDE
直接运行 What2EatApplication.java
```

### 5. 访问

- API地址：http://localhost:8883/api
- API文档：http://localhost:8883/api/swagger-ui.html
- WebSocket：ws://localhost:8883/api/ws

## 测试账号

- 用户名：testuser1
- 密码：123456

## API文档

启动后访问：http://localhost:8883/api/swagger-ui.html

## 主要功能

- ✅ 用户注册/登录（JWT认证）
- ✅ 菜品管理（上传/查询/删除）
- ✅ 推送记录（推送/查询/删除）
- ✅ 好友管理（添加/删除/查询）
- ✅ 文件上传（图片）
- ✅ WebSocket实时推送

## 技术栈

- Spring Boot 3.2.1
- Spring Data JPA
- Spring WebSocket
- MySQL 8.0
- JWT
- Maven
