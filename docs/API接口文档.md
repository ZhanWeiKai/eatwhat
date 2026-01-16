# 今天吃什么 - API接口文档

## 基本信息

- **基础URL**: `http://localhost:8883/api`
- **API文档**: http://localhost:8883/api/swagger-ui.html
- **WebSocket**: `ws://localhost:8883/api/ws`
- **认证方式**: JWT Bearer Token

## 统一响应格式

所有接口返回统一的JSON格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（Token无效或过期） |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

---

## 认证接口

### 1. 用户注册

**接口地址**: `POST /auth/register`

**请求参数**:
```json
{
  "username": "testuser",
  "password": "123456",
  "nickname": "测试用户"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "user123",
    "username": "testuser",
    "nickname": "测试用户"
  }
}
```

### 2. 用户登录

**接口地址**: `POST /auth/login`

**请求参数**:
```json
{
  "username": "testuser",
  "password": "123456"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "user123",
    "username": "testuser",
    "nickname": "测试用户",
    "avatar": "http://localhost:8883/api/static/default-avatar.png"
  }
}
```

### 3. 获取当前用户信息

**接口地址**: `GET /auth/me`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": "user123",
    "username": "testuser",
    "nickname": "测试用户",
    "avatar": "http://localhost:8883/api/static/default-avatar.png",
    "createdAt": "2026-01-15T10:30:00"
  }
}
```

### 4. 退出登录

**接口地址**: `POST /auth/logout`

**响应示例**:
```json
{
  "code": 200,
  "message": "退出成功",
  "data": null
}
```

---

## 菜品接口

### 1. 获取所有菜品

**接口地址**: `GET /dishes`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "dishId": "dish001",
      "name": "麻婆豆腐",
      "description": "经典川菜，麻辣鲜香",
      "price": 28.00,
      "category": "热菜",
      "imageUrl": "http://localhost:8883/api/static/mapo_tofu.jpg",
      "uploaderId": "user001",
      "createdAt": "2026-01-15T10:00:00"
    }
  ]
}
```

### 2. 根据分类获取菜品

**接口地址**: `GET /dishes/category/{category}`

**路径参数**:
- `category`: 分类名称（热菜/凉菜/主食/汤品/饮品）

**响应示例**: 同上

### 3. 根据ID获取菜品

**接口地址**: `GET /dishes/{id}`

**路径参数**:
- `id`: 菜品ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dishId": "dish001",
    "name": "麻婆豆腐",
    "description": "经典川菜，麻辣鲜香",
    "price": 28.00,
    "category": "热菜",
    "imageUrl": "http://localhost:8883/api/static/mapo_tofu.jpg",
    "uploaderId": "user001"
  }
}
```

### 4. 上传菜品

**接口地址**: `POST /dishes`

**请求头**:
```
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "name": "宫保鸡丁",
  "description": "酸甜微辣，口感丰富",
  "price": 38.00,
  "category": "热菜",
  "imageUrl": "http://localhost:8883/api/static/kungpao_chicken.jpg"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "dishId": "dish002",
    "name": "宫保鸡丁",
    "description": "酸甜微辣，口感丰富",
    "price": 38.00,
    "category": "热菜",
    "imageUrl": "http://localhost:8883/api/static/kungpao_chicken.jpg",
    "uploaderId": "user123",
    "createdAt": "2026-01-15T11:00:00"
  }
}
```

### 5. 删除菜品

**接口地址**: `DELETE /dishes/{id}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 6. 获取所有分类

**接口地址**: `GET /dishes/categories/all`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": ["热菜", "凉菜", "主食", "汤品", "饮品"]
}
```

---

## 推送接口

### 1. 获取所有推送记录

**接口地址**: `GET /push/list`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "pushId": "push001",
      "pusherId": "user001",
      "pusherName": "测试用户1",
      "pusherAvatar": "http://localhost:8883/api/static/avatar1.jpg",
      "dishes": [
        {
          "dishId": "dish001",
          "name": "麻婆豆腐",
          "price": 28.00,
          "quantity": 1,
          "imageUrl": "http://localhost:8883/api/static/mapo_tofu.jpg"
        }
      ],
      "totalAmount": 28.00,
      "createdAt": "2026-01-15T12:00:00"
    }
  ]
}
```

### 2. 根据ID获取推送记录

**接口地址**: `GET /push/{id}`

**路径参数**:
- `id`: 推送记录ID

**响应示例**: 同上

### 3. 推送菜单

**接口地址**: `POST /push`

**请求头**:
```
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "pusherName": "测试用户1",
  "pusherAvatar": "http://localhost:8883/api/static/avatar1.jpg",
  "dishes": [
    {
      "dishId": "dish001",
      "name": "麻婆豆腐",
      "price": 28.00,
      "quantity": 1,
      "imageUrl": "http://localhost:8883/api/static/mapo_tofu.jpg"
    },
    {
      "dishId": "dish002",
      "name": "宫保鸡丁",
      "price": 38.00,
      "quantity": 2,
      "imageUrl": "http://localhost:8883/api/static/kungpao_chicken.jpg"
    }
  ]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "推送成功",
  "data": {
    "pushId": "push002",
    "pusherId": "user123",
    "pusherName": "测试用户1",
    "pusherAvatar": "http://localhost:8883/api/static/avatar1.jpg",
    "dishes": [
      {
        "dishId": "dish001",
        "name": "麻婆豆腐",
        "price": 28.00,
        "quantity": 1
      },
      {
        "dishId": "dish002",
        "name": "宫保鸡丁",
        "price": 38.00,
        "quantity": 2
      }
    ],
    "totalAmount": 104.00,
    "createdAt": "2026-01-15T12:30:00"
  }
}
```

**WebSocket推送**:
当有新推送时，服务器会通过WebSocket推送到 `/topic/push`，所有订阅的客户端都会实时收到推送通知。

### 4. 删除推送

**接口地址**: `DELETE /push/{id}`

**请求头**:
```
Authorization: Bearer {token}
```

**权限说明**: 只有推送者本人可以删除自己的推送记录

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**错误示例**:
```json
{
  "code": 500,
  "message": "无权删除此推送",
  "data": null
}
```

---

## 好友接口

### 1. 获取好友列表

**接口地址**: `GET /friends`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "friend001",
      "userId": "user123",
      "friendId": "user456",
      "createdAt": "2026-01-15T10:00:00"
    }
  ]
}
```

### 2. 添加好友

**接口地址**: `POST /friends/add`

**请求头**:
```
Authorization: Bearer {token}
```

**请求参数**:
```
friendId=user456
```

**响应示例**:
```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": "friend002",
    "userId": "user123",
    "friendId": "user456",
    "createdAt": "2026-01-15T14:00:00"
  }
}
```

### 3. 删除好友

**接口地址**: `DELETE /friends/{friendId}`

**请求头**:
```
Authorization: Bearer {token}
```

**路径参数**:
- `friendId`: 好友ID

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 文件上传接口

### 1. 上传图片

**接口地址**: `POST /upload/image`

**请求类型**: `multipart/form-data`

**请求参数**:
```
file: (binary) 图片文件
```

**限制**:
- 文件类型：只能上传图片（image/*）
- 文件大小：最大10MB
- 支持格式：jpg, png, gif, webp

**响应示例**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "http://localhost:8883/api/static/abc123-def456.jpg"
  }
}
```

**使用示例**:
```bash
curl -X POST http://localhost:8883/api/upload/image \
  -F "file=@/path/to/image.jpg"
```

---

## WebSocket接口

### 连接端点

**URL**: `ws://localhost:8883/api/ws`

**协议**: STOMP over WebSocket

### 订阅推送消息

**订阅地址**: `/topic/push`

**消息格式**:
```json
{
  "pushId": "push001",
  "pusherId": "user001",
  "pusherName": "测试用户1",
  "dishes": [...],
  "totalAmount": 104.00,
  "createdAt": "2026-01-15T12:30:00"
}
```

### 客户端连接示例（JavaScript）

```javascript
// 创建WebSocket连接
const socket = new SockJS('http://localhost:8883/api/ws');
const stompClient = Stomp.over(socket);

// 连接
stompClient.connect({}, function(frame) {
  console.log('Connected: ' + frame);

  // 订阅推送消息
  stompClient.subscribe('/topic/push', function(message) {
    const push = JSON.parse(message.body);
    console.log('收到新推送:', push);

    // 更新UI
    updatePushList(push);
  });
});
```

### Android客户端连接示例

```java
// 使用OkHttp WebSocket
String wsUrl = "ws://192.168.1.100:8883/api/ws";
OkHttpClient client = new OkHttpClient();

Request request = new Request.Builder()
    .url(wsUrl)
    .build();

WebSocketListener listener = new WebSocketListener() {
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        // 连接成功
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        // 收到消息
        Push push = new Gson().fromJson(text, Push.class);
        runOnUiThread(() -> {
            // 更新UI
            updatePushList(push);
        });
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        // 连接失败
    }
};

WebSocket ws = client.newWebSocket(request, listener);
```

---

## 测试账号

| 用户名 | 密码 | 昵称 | UserID |
|--------|------|------|--------|
| testuser1 | 123456 | 测试用户1 | user001 |
| testuser2 | 123456 | 测试用户2 | user002 |
| testuser3 | 123456 | 测试用户3 | user003 |

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | Token无效或过期 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 409 | 资源冲突（如用户名已存在） |
| 500 | 服务器内部错误 |

---

## 使用Postman测试

### 导入集合

1. 打开Postman
2. 点击 Import
3. 选择以下JSON导入（或手动添加请求）

### 测试流程

**1. 注册新用户**
```
POST http://localhost:8883/api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "123456",
  "nickname": "新用户"
}
```

**2. 登录**
```
POST http://localhost:8883/api/auth/login
Content-Type: application/json

{
  "username": "testuser1",
  "password": "123456"
}
```

**3. 获取菜品列表**
```
GET http://localhost:8883/api/dishes
Authorization: Bearer {token}
```

**4. 推送菜单**
```
POST http://localhost:8883/api/push
Authorization: Bearer {token}
Content-Type: application/json

{
  "pusherName": "测试用户1",
  "dishes": [
    {
      "dishId": "dish001",
      "name": "麻婆豆腐",
      "price": 28.00,
      "quantity": 1
    }
  ]
}
```

---

## 在线API文档

启动后端服务后，访问以下地址查看完整的Swagger文档：

- **Swagger UI**: http://localhost:8883/api/swagger-ui.html
- **API Docs JSON**: http://localhost:8883/api/api-docs

---

**文档版本**: v1.0
**最后更新**: 2026-01-15
