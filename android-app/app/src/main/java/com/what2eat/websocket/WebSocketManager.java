package com.what2eat.websocket;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.what2eat.R;
import com.what2eat.data.model.Push;
import com.what2eat.ui.push.PushListActivity;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * WebSocket连接管理器
 * 使用OkHttp原生WebSocket实现，直接处理STOMP协议
 */
public class WebSocketManager {

    private static final String TAG = "WebSocketManager";
    private static final String WS_URL = "ws://api.jamesweb.org:8883/api/ws";

    private OkHttpClient client;
    private WebSocket webSocket;
    private String userId;
    private Context context;
    private Gson gson;
    private boolean isConnected = false;

    public WebSocketManager(String userId, Context context) {
        this.userId = userId;
        this.context = context.getApplicationContext();
        this.gson = new Gson();

        // 创建高优先级推送通知渠道
        createPushNotificationChannel();

        // 创建OkHttpClient
        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .pingInterval(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 创建高优先级推送通知渠道
     */
    private void createPushNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "push_channel",
                    "菜单推送",
                    NotificationManager.IMPORTANCE_HIGH  // 高重要性，会弹出横幅
            );
            channel.setDescription("接收好友推送的菜单消息");
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setShowBadge(true);
            // 华为设备：设置振动模式
            channel.setVibrationPattern(new long[]{0, 300, 200, 300});
            // 锁屏可见
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * 连接WebSocket
     */
    public void connect() {
        if (isConnected) {
            Log.w(TAG, "WebSocket already connected");
            return;
        }

        Log.i(TAG, "Connecting to WebSocket...");

        Request request = new Request.Builder()
                .url(WS_URL)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.i(TAG, "WebSocket connected successfully");
                isConnected = true;

                // 连接成功后发送STOMP CONNECT命令
                String connectFrame = "CONNECT\naccept-version:1.2,1.1,1.0\nheart-beat:30000,30000\n\n\u0000";
                webSocket.send(connectFrame);

                // 订阅用户专属频道（接收推送消息）
                String subscribeFrame = "SUBSCRIBE\nid:sub-0\ndestination:/topic/user/" + userId + "\n\n\u0000";
                webSocket.send(subscribeFrame);

                // 订阅用户状态频道（接收好友在线状态变化）
                String statusSubscribeFrame = "SUBSCRIBE\nid:sub-status\ndestination:/topic/user-status\n\n\u0000";
                webSocket.send(statusSubscribeFrame);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "Received message: " + text);

                // 处理STOMP消息
                if (text.startsWith("CONNECTED")) {
                    Log.i(TAG, "STOMP connection established");
                } else if (text.contains("MESSAGE")) {
                    // 检查是否是用户状态消息
                    if (text.contains("user-status")) {
                        handleUserStatusMessage(text);
                        return;
                    }

                    // 否则按推送消息处理
                    String body = extractStompBody(text);
                    if (body != null && !body.isEmpty()) {
                        try {
                            Push push = gson.fromJson(body, Push.class);
                            showPushNotification(push);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing push message: " + e.getMessage());
                        }
                    }
                }
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.w(TAG, "WebSocket closing: " + reason);
                isConnected = false;
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.w(TAG, "WebSocket closed: " + reason);
                isConnected = false;
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "WebSocket error: " + t.getMessage(), t);
                isConnected = false;
                // 尝试重连
                reconnect();
            }
        });
    }

    /**
     * 提取STOMP消息体
     */
    private String extractStompBody(String stompMessage) {
        // STOMP格式: MESSAGE\ndestination:...\n\n{JSON}\u0000
        int bodyStart = stompMessage.indexOf("\n\n");
        if (bodyStart != -1) {
            String body = stompMessage.substring(bodyStart + 2);
            // 移除结尾的null字符
            if (body.endsWith("\u0000")) {
                body = body.substring(0, body.length() - 1);
            }
            return body.trim();
        }
        return stompMessage;
    }

    /**
     * 显示系统通知（即使应用在后台也能弹出）
     */
    private void showPushNotification(Push push) {
        Log.i(TAG, "Showing notification for push from: " + push.getPusherName());

        // 创建点击通知的Intent
        Intent intent = new Intent(context, PushListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(
                    context,
                    (int) System.currentTimeMillis(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        } else {
            pendingIntent = PendingIntent.getActivity(
                    context,
                    (int) System.currentTimeMillis(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }

        // 构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "push_channel")
                .setContentTitle(push.getPusherName() + "给你推送了菜单")
                .setContentText("总计：" + push.getTotalAmount() + "元，点击查看详情")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                // 华为设备优化：增强通知可见性
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(false)
                .setOnlyAlertOnce(false)
                // 确保有声音和振动
                .setVibrate(new long[]{0, 300, 200, 300});

        // 显示通知
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(push.getPushId().hashCode(), builder.build());
            Log.i(TAG, "Notification displayed successfully");
        }
    }

    /**
     * 处理用户状态消息
     */
    private void handleUserStatusMessage(String stompMessage) {
        try {
            String body = extractStompBody(stompMessage);
            if (body != null && !body.isEmpty()) {
                com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(body).getAsJsonObject();
                String statusUserId = json.get("userId").getAsString();
                int status = json.get("status").getAsInt();
                boolean isOnline = (status == 1);

                Log.i(TAG, "User status changed: " + statusUserId + " is now " + (isOnline ? "ONLINE" : "OFFLINE"));

                // 这里可以使用LocalBroadcastManager或EventBus通知UI更新
                // 暂时只记录日志，因为FriendListActivity已经在后台监听
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing user status message: " + e.getMessage());
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (webSocket != null) {
            Log.i(TAG, "Disconnecting WebSocket...");
            // 发送DISCONNECT帧
            String disconnectFrame = "DISCONNECT\n\n\u0000";
            webSocket.send(disconnectFrame);
            webSocket.close(1000, "User disconnect");
            webSocket = null;
            isConnected = false;
        }
    }

    /**
     * 重连
     */
    private void reconnect() {
        if (isConnected) {
            return;
        }

        Log.i(TAG, "Attempting to reconnect in 3 seconds...");

        // 延迟3秒后重连
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isConnected) {
                    Log.i(TAG, "Reconnecting...");
                    connect();
                }
            }
        }, 3000);
    }

    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return isConnected;
    }
}
