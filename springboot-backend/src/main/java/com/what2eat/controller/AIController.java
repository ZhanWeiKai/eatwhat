package com.what2eat.controller;

import com.what2eat.dto.response.ApiResponse;
import com.what2eat.dto.ChatRequest;
import com.what2eat.service.ZhipuAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AI聊天控制器
 */
@RestController
@RequestMapping("/ai")
@Slf4j
public class AIController {

    @Autowired
    private ZhipuAIService zhipuAIService;

    /**
     * AI聊天接口
     */
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<Map<String, String>>> chat(@RequestBody ChatRequest request) {
        try {
            log.info("收到AI聊天请求: {}", request.getMessage());

            // 调用AI服务
            String aiResponse = zhipuAIService.chat(request.getMessage());

            // 构建响应
            Map<String, String> data = new HashMap<>();
            data.put("message", aiResponse);

            return ResponseEntity.ok(ApiResponse.success("成功", data));

        } catch (Exception e) {
            log.error("AI聊天失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "AI服务暂时不可用"));
        }
    }
}
