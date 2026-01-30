package com.what2eat.utils;

import com.what2eat.enums.SSEMsgType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * SSE服务管理类
 * 负责管理所有SSE连接和消息发送
 */
@Slf4j
public class SSEServer {

    // 存放所有用户的SSE连接
    private static final Map<String, SseEmitter> sseClients = new ConcurrentHashMap<>();

    /**
     * 建立SSE连接
     *
     * @param userId 用户ID
     * @return SseEmitter
     */
    public static SseEmitter connect(String userId) {
        // 设置超时时间为0（永不过期），默认30秒
        SseEmitter sseEmitter = new SseEmitter(0L);

        // 注册回调
        sseEmitter.onTimeout(timeoutCallback(userId));
        sseEmitter.onCompletion(completionCallback(userId));
        sseEmitter.onError(errorCallback(userId));

        sseClients.put(userId, sseEmitter);

        log.info("SSE连接创建成功，用户ID: {}", userId);

        return sseEmitter;
    }

    /**
     * 发送消息给指定用户
     *
     * @param userId  用户ID
     * @param message 消息内容
     * @param msgType 消息类型
     */
    public static void sendMsg(String userId, String message, SSEMsgType msgType) {
        if (CollectionUtils.isEmpty(sseClients)) {
            return;
        }

        if (sseClients.containsKey(userId)) {
            SseEmitter sseEmitter = sseClients.get(userId);
            sendEmitterMessage(sseEmitter, userId, message, msgType);
        } else {
            log.warn("用户{}的SSE连接不存在", userId);
        }
    }

    /**
     * 发送消息给所有用户
     *
     * @param message 消息内容
     */
    public static void sendMsgToAllUsers(String message) {
        if (CollectionUtils.isEmpty(sseClients)) {
            return;
        }

        sseClients.forEach((userId, sseEmitter) ->
                sendEmitterMessage(sseEmitter, userId, message, SSEMsgType.CONNECT)
        );
    }

    /**
     * 实际发送SSE消息
     */
    private static void sendEmitterMessage(SseEmitter sseEmitter,
                                          String userId,
                                          String message,
                                          SSEMsgType msgType) {
        try {
            SseEmitter.SseEventBuilder msgEvent = SseEmitter.event()
                    .id(userId)
                    .data(message)
                    .name(msgType.type);
            sseEmitter.send(msgEvent);
            log.debug("SSE消息发送成功，用户: {}, 类型: {}, 内容: {}", userId, msgType.type, message);
        } catch (IOException e) {
            log.error("SSE消息发送失败，用户: {}, 错误: {}", userId, e.getMessage());
            remove(userId);
        }
    }

    /**
     * 错误回调
     */
    private static Consumer<Throwable> errorCallback(String userId) {
        return throwable -> {
            log.error("SSE连接异常，用户: {}, 错误: {}", userId, throwable.getMessage());
            remove(userId);
        };
    }

    /**
     * 超时回调
     */
    private static Runnable timeoutCallback(String userId) {
        return () -> {
            log.warn("SSE连接超时，用户: {}", userId);
            remove(userId);
        };
    }

    /**
     * 完成回调
     */
    private static Runnable completionCallback(String userId) {
        return () -> {
            log.info("SSE连接完成，用户: {}", userId);
            remove(userId);
        };
    }

    /**
     * 移除用户连接
     *
     * @param userId 用户ID
     */
    public static void remove(String userId) {
        sseClients.remove(userId);
        log.info("SSE连接已移除，用户: {}", userId);
    }

    /**
     * 获取当前连接数
     */
    public static int getConnectionCount() {
        return sseClients.size();
    }
}
