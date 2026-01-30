# WebSocket推送消息完整流程

## 一、完整流程图

```
┌─────────────────────────────────────────────────────────────────┐
│                      推送消息流程                                │
└─────────────────────────────────────────────────────────────────┘

用户A推送给用户B：

用户A的手机（Android）          后端服务器                用户B的手机（Android）
┌──────────────────┐           ┌──────────────┐         ┌──────────────────┐
│  MenuActivity    │           │              │         │  (前台/后台)     │
│  - 选择菜品      │           │              │         │                  │
│  - 点击"推送给好友"│  HTTP POST │              │         │ WebSocketService │
└────────┬─────────┘           │              │         │  (保持WebSocket   │
         │                     │              │         │   连接)           │
         │  /api/push/create   │              │         └────────┬─────────┘
         └─────────────────────>│              │                  │
                              │  PushService │                  │
                              │              │                  │
                              │  1. 查询好友关系 │
                              │  - 我关注的      │
                              │  - 关注我的     │
                              │  2. 合并+去重    │
                              │  3. 排除自己      │
                              │              │
                              │  WebSocket推送 │
                              │              │
                              └──────────────┼──────────────────>│
                                             /topic/user/userB
                                             │
                                             │  WebSocketManager
                                             │  - 收到MESSAGE
                                             │  - 解析JSON
                                             │  - 显示通知
```

## 二、关键代码和配置

### 1. 后端：WebSocket配置

**文件**: `src/main/java/com/what2eat/config/WebSocketConfig.java`

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理，用于推送消息给客户端
        config.enableSimpleBroker("/topic");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 原生WebSocket端点（用于Android客户端）
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");

        // SockJS端点（用于Web客户端）
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
```

**说明**：
- WebSocket端点：`ws://api.jamesweb.org:8883/api/ws`
- 消息代理前缀：`/topic`
- 用户频道：`/topic/user/{userId}`

### 2. 后端：推送服务

**文件**: `src/main/java/com/what2eat/service/PushService.java`

```java
@Service
public class PushService {

    @Transactional
    public Push createPush(Push push, String pusherId) {
        // 1. 保存推送记录
        push.setPushId(UUID.randomUUID().toString());
        push.setPusherId(pusherId);
        push.setTotalAmount(计算总金额);
        Push savedPush = pushRepository.save(push);

        // 2. 查询双向好友关系
        List<Friendship> iFollow = friendshipRepository.findByUserId(pusherId);    // 我关注的
        List<Friendship> followMe = friendshipRepository.findByFriendId(pusherId);  // 关注我的

        // 3. 合并好友ID并去重
        Set<String> friendIdSet = new HashSet<>();
        for (Friendship f : iFollow) {
            friendIdSet.add(f.getFriendId());    // 我关注的人的ID
        }
        for (Friendship f : followMe) {
            friendIdSet.add(f.getUserId());       // 关注我的人的ID
        }

        // 4. 排除自己（不要给自己发推送）
        friendIdSet.remove(pusherId);

        // 5. 推送给每个好友
        for (String friendId : friendIdSet) {
            messagingTemplate.convertAndSend(
                "/topic/user/" + friendId,  // 推送到该好友的专属频道
                savedPush
            );
            System.out.println("推送消息已发送给用户: " + friendId);
        }

        return savedPush;
    }
}
```

**关键逻辑**：
1. ✅ 查询双向好友关系
2. ✅ 合并去重
3. ✅ 排除自己
4. ✅ 推送到每个好友的专属频道

### 3. Android：WebSocket前台服务

**文件**: `app/src/main/java/com/what2eat/service/WebSocketService.java`

```java
public class WebSocketService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        // 创建通知渠道（最低优先级，不显示在状态栏）
        createNotificationChannel();

        // 启动前台服务（必须显示通知）
        startForeground(NOTIFICATION_ID, createForegroundNotification());

        // 获取保存的用户ID
        String userId = getUserId();  // 从SharedPreferences读取

        // 初始化WebSocket连接
        webSocketManager = new WebSocketManager(userId, this);
        webSocketManager.connect();
    }
}
```

**说明**：
- 前台服务保持应用存活
- 必须显示通知（Android系统强制要求）
- 最低优先级（IMPORTANCE_MIN）

### 4. Android：WebSocket管理器

**文件**: `app/src/main/java/com/what2eat/websocket/WebSocketManager.java`

```java
public class WebSocketManager {
    private static final String WS_URL = "ws://api.jamesweb.org:8883/api/ws";

    public void connect() {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .pingInterval(30, TimeUnit.SECONDS)  // 30秒心跳
                .build();

        Request request = new Request.Builder().url(WS_URL).build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                isConnected = true;

                // 发送STOMP CONNECT命令
                String connectFrame = "CONNECT\\naccept-version:1.2,1.1,1.0\\n\\n\\u0000";
                webSocket.send(connectFrame);

                // 订阅用户专属频道
                String subscribeFrame = "SUBSCRIBE\\nid:sub-0\\ndestination:/topic/user/" + userId + "\\n\\n\\u0000";
                webSocket.send(subscribeFrame);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // 处理STOMP消息
                if (text.contains("MESSAGE")) {
                    String body = extractStompBody(text);  // 提取JSON
                    Push push = gson.fromJson(body, Push.class);
                    showPushNotification(push);  // 显示通知
                }
            }
        });
    }

    private void showPushNotification(Push push) {
        // 创建高优先级通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "push_channel")
                .setContentTitle(push.getPusherName() + "给你推送了菜单")
                .setContentText("总计：" + push.getTotalAmount() + "元，点击查看详情")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // 高优先级，弹出横幅
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(push.getPushId().hashCode(), builder.build());
    }
}
```

**关键步骤**：
1. ✅ 连接WebSocket
2. ✅ 发送STOMP CONNECT
3. ✅ 订阅 `/topic/user/{userId}`
4. ✅ 接收MESSAGE消息
5. ✅ 显示通知

### 5. Android：MainActivity启动服务

**文件**: `app/src/main/java/com/what2eat/ui/main/MainActivity.java`

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // 启动WebSocket前台服务
    startWebSocketService();
}

private void startWebSocketService() {
    Intent serviceIntent = new Intent(this, WebSocketService.class);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(serviceIntent);
    } else {
        startService(serviceIntent);
    }
}
```

**说明**：
- 应用启动时自动启动WebSocketService
- SharedPreferences保存了userId，Service会自动读取并连接

## 三、数据库表结构

### 1. 用户表 (user)

| 字段 | 说明 | 示例 |
|------|------|------|
| user_id | 用户ID | user001, user002 |
| username | 用户名 | testuser1, testuser2 |
| nickname | 昵称 | 测试用户1, 测试用户2 |

### 2. 好友关系表 (friendship)

| 字段 | 说明 | 示例 |
|------|------|------|
| user_id | 关注者ID | user002 |
| friend_id | 被关注者ID | user001 |

**示例数据**：
```
user002 → user001  (user002关注了user001)
user001 → user002  (user001也关注了user002)
```

## 四、当前问题诊断

### 检查清单

#### ✅ 后端检查
1. WebSocket端点可访问：`curl -i -N -H "Upgrade: websocket" http://api.jamesweb.org:8883/api/ws`
2. 返回HTTP 101（握手成功）

#### ✅ Android连接检查
从日志看到：
```
I/WebSocketManager(25332): WebSocket connected successfully
I/WebSocketManager(25332): STOMP connection established
```
说明WebSocket连接正常

#### ⚠️ 当前可能的问题

**问题A：应用被关闭**
- 华为手机上应用被关闭（`com.what2eat become invisible`）
- WebSocketService被杀死
- 无法接收推送

**问题B：userId不匹配**
- SharedPreferences保存的userId
- 数据库中的userId
- 需要确保一致

**问题C：后端推送日志缺失**
- 需要检查后端是否真的发送了推送消息
- 需要查看后端日志

## 五、调试步骤

### 步骤1：确认两端都打开应用

1. **打开华为手机的应用**（testuser1）
2. **打开安卓手机的应用**（testuser2）
3. **保持应用在前台或后台**（不要关闭）

### 步骤2：监控WebSocket连接

```bash
# 华为手机
adb -s 华为设备ID logcat | grep "WebSocket"

# 安卓手机
adb -s 安卓设备ID logcat | grep "WebSocket"
```

**应该看到**：
```
I/WebSocketService: WebSocket manager initialized for user: user001/user002
I/WebSocketManager: WebSocket connected successfully
I/WebSocketManager: STOMP connection established
```

### 步骤3：执行推送操作

1. testuser2推送菜单给testuser1
2. 查看后端日志

### 步骤4：检查后端日志

```bash
ssh root@47.242.74.112 "cd /root/what2eat && docker compose logs -f app | grep '推送'"
```

**应该看到**：
```
推送消息已发送给用户: user001
推送消息已发送给 1/1 个好友
```

### 步骤5：检查Android接收日志

**testuser1的手机应该看到**：
```
D/WebSocketManager: Received message: MESSAGE
I/WebSocketManager: Showing notification for push from: testuser2
I/WebSocketManager: Notification displayed successfully
```

## 六、常见问题

### Q1：后端没有推送日志
**原因**：
- 推送API调用失败
- 好友关系查询失败

**解决**：
检查后端日志，查看是否有错误信息

### Q2：Android没有连接日志
**原因**：
- userId未保存到SharedPreferences
- WebSocketService未启动

**解决**：
```bash
adb shell "run-as com.what2eat cat /data/data/com.what2eat/shared_prefs/What2Eat.xml"
```

确认userId存在

### Q3：连接成功但收不到消息
**原因**：
- 订阅的频道不匹配
- userId不一致

**解决**：
确认订阅的是 `/topic/user/{正确的userId}`

## 七、下一步行动

请按照上述调试步骤操作，并告诉我：

1. **两台手机的WebSocket连接日志**（是否都连接成功）
2. **推送操作时的后端日志**（是否发送了推送）
3. **接收端的Android日志**（是否收到MESSAGE）

这样我才能准确定位问题！
