package com.what2eat.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.what2eat.R;
import com.what2eat.ui.push.PushListActivity;
import com.what2eat.websocket.WebSocketManager;

/**
 * WebSocket前台服务
 * 保持WebSocket长连接，接收推送消息并显示通知
 */
public class WebSocketService extends Service {

    private static final String TAG = "WebSocketService";
    private static final String SERVICE_CHANNEL_ID = "service_channel";  // 前台服务通知渠道
    private static final int NOTIFICATION_ID = 1001;

    private final IBinder binder = new LocalBinder();
    private WebSocketManager webSocketManager;

    /**
     * Local Binder for clients to bind to this service
     */
    public class LocalBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "WebSocketService creating...");

        // 创建通知渠道（Android 8.0+必需）
        createNotificationChannel();

        // 启动前台服务
        startForeground(NOTIFICATION_ID, createForegroundNotification());

        // 获取保存的用户ID
        String userId = getUserId();
        if (!userId.isEmpty()) {
            // 初始化WebSocket连接
            webSocketManager = new WebSocketManager(userId, this);
            webSocketManager.connect();
            Log.i(TAG, "WebSocket manager initialized for user: " + userId);
        } else {
            Log.w(TAG, "No user ID found, skipping WebSocket connection");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "WebSocketService started");
        // START_STICKY: 服务被杀死后自动重启
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "WebSocketService destroying...");
        if (webSocketManager != null) {
            webSocketManager.disconnect();
        }
        super.onDestroy();
    }

    /**
     * 创建通知渠道（Android 8.0+）
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 前台服务通知渠道 - 最低重要性
            NotificationChannel channel = new NotificationChannel(
                    SERVICE_CHANNEL_ID,
                    "后台服务",
                    NotificationManager.IMPORTANCE_MIN  // 最低重要性，不显示在状态栏和通知栏
            );
            channel.setDescription("保持后台连接");
            channel.enableVibration(false);
            channel.enableLights(false);
            channel.setShowBadge(false);  // 不显示角标
            channel.setSound(null, null);  // 无声音
            channel.setBypassDnd(false);  // 不绕过勿扰模式

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.i(TAG, "Notification channel created with IMPORTANCE_MIN");
            }
        }
    }

    /**
     * 创建前台服务通知
     */
    private Notification createForegroundNotification() {
        // 点击通知打开推送列表
        Intent intent = new Intent(this, PushListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        } else {
            pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }

        return new NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
                .setContentTitle("今天吃什么")
                .setContentText("后台运行中，接收好友推送")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_MIN)  // 最低优先级
                .setOngoing(true)  // 不可滑动删除
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)  // 不显示在锁屏
                .setAutoCancel(false)  // 不自动取消
                .setSilent(true)  // 静音
                .build();
    }

    /**
     * 从SharedPreferences获取用户ID
     */
    private String getUserId() {
        android.content.SharedPreferences prefs = getSharedPreferences("What2Eat", Context.MODE_PRIVATE);
        return prefs.getString("userId", "");
    }

    /**
     * 获取WebSocketManager实例
     */
    public WebSocketManager getWebSocketManager() {
        return webSocketManager;
    }
}
