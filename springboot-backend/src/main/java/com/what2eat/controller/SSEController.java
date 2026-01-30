package com.what2eat.controller;

import com.what2eat.dto.ChatRequest;
import com.what2eat.dto.response.ApiResponse;
import com.what2eat.service.ZhipuAIService;
import com.what2eat.utils.SSEServer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * SSE流式聊天控制器
 */
@RestController
@RequestMapping("/sse")
@Slf4j
public class SSEController {

    @Resource
    private ZhipuAIService zhipuAIService;

    /**
     * 建立SSE连接
     *
     * @param userId 用户ID
     * @return SseEmitter
     */
    @GetMapping(path = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam String userId) {
        log.info("用户请求建立SSE连接: {}", userId);
        SseEmitter emitter = SSEServer.connect(userId);

        try {
            // 发送连接成功确认消息
            emitter.send(SseEmitter.event()
                    .id(userId)
                    .data("connected")
                    .name("connect"));
        } catch (Exception e) {
            log.error("发送连接确认消息失败", e);
        }

        return emitter;
    }

    /**
     * 流式聊天接口
     *
     * @param request  聊天请求
     * @param userId  用户ID
     * @return 响应
     */
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<Map<String, String>>> chatStream(
            @RequestBody ChatRequest request,
            @RequestParam String userId) {

        log.info("收到流式聊天请求，用户: {}, 消息: {}", userId, request.getMessage());

        // 异步执行流式AI回复
        CompletableFuture.runAsync(() -> {
            zhipuAIService.chatStream(request.getMessage(), userId);
        });

        Map<String, String> data = new HashMap<>();
        data.put("status", "streaming");
        data.put("message", "流式对话已开始");

        return ResponseEntity.ok(ApiResponse.success("成功", data));
    }

    /**
     * 获取SSE连接统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getStats() {
        Map<String, Integer> data = new HashMap<>();
        data.put("connectionCount", SSEServer.getConnectionCount());

        return ResponseEntity.ok(ApiResponse.success("成功", data));
    }
}
