package com.what2eat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.what2eat.config.ZhipuAIConfig;
import com.what2eat.dto.ChatMessage;
import com.what2eat.dto.ZhipuAIRequest;
import com.what2eat.dto.ZhipuAIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 智谱AI服务
 */
@Service
@Slf4j
public class ZhipuAIService {

    @Autowired
    private ZhipuAIConfig config;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 发送聊天消息到AI
     *
     * @param userMessage 用户消息
     * @return AI回复
     */
    public String chat(String userMessage) {
        try {
            // 构建请求
            ZhipuAIRequest request = buildRequest(userMessage);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            // 发送请求
            HttpEntity<ZhipuAIRequest> entity = new HttpEntity<>(request, headers);
            log.info("发送AI请求: {}", userMessage);

            ResponseEntity<String> response = restTemplate.exchange(
                    config.getApiUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 解析响应
            String responseBody = response.getBody();
            log.info("收到AI响应: {}", responseBody);

            JsonNode root = objectMapper.readTree(responseBody);
            String aiMessage = root.path("choices").get(0).path("message").path("content").asText();

            return aiMessage;

        } catch (Exception e) {
            log.error("AI调用失败", e);
            return "抱歉，我现在无法回答，请稍后再试。";
        }
    }

    /**
     * 构建AI请求
     */
    private ZhipuAIRequest buildRequest(String userMessage) {
        List<ChatMessage> messages = new ArrayList<>();

        // 添加系统Prompt
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setRole("system");
        systemMessage.setContent(config.getBasePrompt());
        messages.add(systemMessage);

        // 添加用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        messages.add(userMsg);

        // 构建请求
        ZhipuAIRequest request = new ZhipuAIRequest();
        request.setModel(config.getModel());
        request.setMessages(messages);
        request.setTemperature(0.7);
        request.setTopP(0.9);
        request.setMaxTokens(150);

        return request;
    }
}
