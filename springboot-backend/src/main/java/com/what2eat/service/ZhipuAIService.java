package com.what2eat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.what2eat.config.ZhipuAIConfig;
import com.what2eat.dto.ChatMessage;
import com.what2eat.dto.ZhipuAIRequest;
import com.what2eat.dto.ZhipuAIResponse;
import com.what2eat.enums.SSEMsgType;
import com.what2eat.utils.SSEServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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

            // 打印请求JSON用于调试
            String requestJson = objectMapper.writeValueAsString(request);
            log.info("发送给智谱AI的JSON请求: {}", requestJson);

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

        // 暂时去掉系统Prompt，只保留用户消息
        // TODO: 调试system prompt导致的500错误
        /*
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setRole("system");
        systemMessage.setContent(config.getBasePrompt());
        messages.add(systemMessage);
        */

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
        // request.setMaxTokens(150);  // 暂时去掉，让模型自己决定

        return request;
    }

    /**
     * 流式聊天 - 实时推送AI回复
     *
     * @param userMessage 用户消息
     * @param userId      用户ID
     */
    public void chatStream(String userMessage, String userId) {
        try {
            // 构建请求，启用流式输出
            ZhipuAIRequest request = buildRequest(userMessage);
            request.setStream(true);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            // 发送请求
            HttpEntity<ZhipuAIRequest> entity = new HttpEntity<>(request, headers);
            log.info("发送AI流式请求: {}", userMessage);

            ResponseEntity<String> response = restTemplate.exchange(
                    config.getApiUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 读取流式响应
            BufferedReader reader = new BufferedReader(
                    new java.io.StringReader(response.getBody())
            );

            String line;
            StringBuilder fullContent = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String jsonData = line.substring(6); // 去掉 "data: " 前缀

                    // 跳过 [DONE] 标记
                    if ("[DONE]".equals(jsonData)) {
                        break;
                    }

                    try {
                        // 解析SSE数据
                        JsonNode root = objectMapper.readTree(jsonData);
                        JsonNode choices = root.path("choices");

                        if (choices.isArray() && choices.size() > 0) {
                            JsonNode delta = choices.get(0).path("delta");
                            String content = delta.path("content").asText();

                            if (!content.isEmpty()) {
                                // 实时推送内容
                                SSEServer.sendMsg(userId, content, SSEMsgType.ADD);
                                fullContent.append(content);
                                log.debug("推送AI内容: {}", content);
                            }

                            // 检查是否结束
                            String finishReason = choices.get(0).path("finish_reason").asText();
                            if ("stop".equals(finishReason)) {
                                break;
                            }
                        }
                    } catch (Exception e) {
                        log.error("解析SSE数据失败: {}", jsonData, e);
                    }
                }
            }

            reader.close();

            // 发送完成信号
            SSEServer.sendMsg(userId, fullContent.toString(), SSEMsgType.FINISH);
            log.info("AI流式回复完成，用户: {}, 总长度: {}", userId, fullContent.length());

        } catch (Exception e) {
            log.error("AI流式调用失败，用户: {}", userId, e);
            SSEServer.sendMsg(userId, "抱歉，我现在无法回答，请稍后再试。", SSEMsgType.ERROR);
        }
    }
}
